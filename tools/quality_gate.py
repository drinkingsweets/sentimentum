#!/usr/bin/env python3
import argparse
import json
import re
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass, field
from pathlib import Path


THRESHOLDS = {
    "method_complexity": {"warn": 10, "fail": 20},
    "wmc": {"warn": 30, "fail": 60},
    "cbo": {"warn": 10, "fail": 20},
    "dit": {"warn": 5, "fail": 7},
    "duplication_percent": {"warn": 5.0, "fail": 15.0},
    "class_methods": {"warn": 15, "fail": 20},
}

CONTROL_TOKENS = re.compile(r"\b(if|for|while|case|catch|switch)\b|&&|\|\||\?")
CLASS_PATTERN = re.compile(r"\b(class|interface|enum|record)\s+([A-Z][A-Za-z0-9_]*)\s*([^{]*)\{")
METHOD_PATTERN = re.compile(
    r"(?:public|private|protected)?\s*(?:static\s+)?(?:final\s+)?"
    r"[\w<>\[\], ?]+\s+([a-zA-Z_][A-Za-z0-9_]*)\s*\([^;{}]*\)\s*(?:throws\s+[^{]+)?\{"
)
IMPORT_PATTERN = re.compile(r"^import\s+(?!static)([\w.]+);", re.MULTILINE)


@dataclass
class MethodMetric:
    name: str
    complexity: int
    start_line: int


@dataclass
class ClassMetric:
    name: str
    file: str
    start_line: int
    methods: list[MethodMetric] = field(default_factory=list)
    cbo: int = 0
    dit: int = 0

    @property
    def wmc(self) -> int:
        return sum(method.complexity for method in self.methods)


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    return re.sub(r"//.*", "", text)


def line_number(text: str, index: int) -> int:
    return text.count("\n", 0, index) + 1


def matching_brace(text: str, open_index: int) -> int:
    depth = 0
    for index in range(open_index, len(text)):
        char = text[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return index
    return len(text) - 1


def method_complexity(body: str) -> int:
    return 1 + len(CONTROL_TOKENS.findall(body))


def extract_methods(class_body: str, offset: int, source: str) -> list[MethodMetric]:
    methods = []
    for match in METHOD_PATTERN.finditer(class_body):
        name = match.group(1)
        if name in {"if", "for", "while", "switch", "catch"}:
            continue
        open_index = offset + match.end() - 1
        close_index = matching_brace(source, open_index)
        body = source[open_index:close_index + 1]
        methods.append(MethodMetric(name, method_complexity(body), line_number(source, open_index)))
    return methods


def class_dependency_count(source: str, class_names: set[str], own_class: str) -> int:
    return len({name for name in class_names if name != own_class and re.search(rf"\b{name}\b", source)})


def inheritance_depth(name: str, parents: dict[str, str | None]) -> int:
    depth = 0
    seen = set()
    parent = parents.get(name)
    while parent and parent not in seen:
        seen.add(parent)
        depth += 1
        parent = parents.get(parent)
    return depth


def analyze_classes(files: list[Path], root: Path) -> list[ClassMetric]:
    sources = {file: strip_comments(file.read_text(encoding="utf-8")) for file in files}
    class_names = set()
    parents: dict[str, str | None] = {}

    for source in sources.values():
        for match in CLASS_PATTERN.finditer(source):
            class_names.add(match.group(2))
            tail = match.group(3)
            parent_match = re.search(r"\bextends\s+([A-Z][A-Za-z0-9_]*)", tail)
            parents[match.group(2)] = parent_match.group(1) if parent_match else None

    metrics = []
    for file, source in sources.items():
        for match in CLASS_PATTERN.finditer(source):
            name = match.group(2)
            open_index = match.end() - 1
            close_index = matching_brace(source, open_index)
            body = source[open_index:close_index + 1]
            metric = ClassMetric(
                name=name,
                file=str(file.relative_to(root)),
                start_line=line_number(source, match.start()),
                methods=extract_methods(body, open_index, source),
                cbo=class_dependency_count(source, class_names, name),
                dit=inheritance_depth(name, parents),
            )
            metrics.append(metric)
    return metrics


def normalized_code_lines(files: list[Path]) -> list[str]:
    lines = []
    for file in files:
        source = strip_comments(file.read_text(encoding="utf-8"))
        for line in source.splitlines():
            normalized = re.sub(r"\s+", " ", line.strip())
            if not normalized or normalized in {"{", "}", "};"}:
                continue
            if normalized.startswith(("package ", "import ", "@")):
                continue
            if re.match(r"^(public|private|protected)?\s*[\w<>\[\]]+\s+get[A-Z]\w*\(\)\s*\{", normalized):
                continue
            if re.match(r"^(public|private|protected)?\s*void\s+set[A-Z]\w*\(", normalized):
                continue
            if normalized.startswith(("return ", "this.")):
                continue
            if normalized in {"super();", "return;", ")", "});"}:
                continue
            if normalized:
                lines.append(normalized)
    return lines


def duplication_percent(files: list[Path], block_size: int = 10) -> float:
    lines = normalized_code_lines(files)
    if len(lines) < block_size:
        return 0.0
    blocks = ["\n".join(lines[index:index + block_size]) for index in range(len(lines) - block_size + 1)]
    counts = Counter(blocks)
    duplicated_lines = sum((count - 1) * block_size for count in counts.values() if count > 1)
    return min(100.0, duplicated_lines * 100.0 / max(1, len(lines)))


def status(value: float, metric: str, lower_is_better: bool = True) -> str:
    threshold = THRESHOLDS[metric]
    if lower_is_better:
        if value > threshold["fail"]:
            return "FAIL"
        if value > threshold["warn"]:
            return "WARN"
        return "OK"
    if value < threshold["fail"]:
        return "FAIL"
    if value < threshold["warn"]:
        return "WARN"
    return "OK"


def build_report(root: Path, class_metrics: list[ClassMetric], duplication: float) -> tuple[dict, str]:
    methods = [method for cls in class_metrics for method in cls.methods]
    max_method = max(methods, key=lambda item: item.complexity, default=MethodMetric("-", 0, 0))
    max_class = max(class_metrics, key=lambda item: item.wmc, default=None)
    max_cbo = max(class_metrics, key=lambda item: item.cbo, default=None)
    max_dit = max(class_metrics, key=lambda item: item.dit, default=None)
    max_methods = max(class_metrics, key=lambda item: len(item.methods), default=None)

    checks = [
        ("Max method cyclomatic complexity", max_method.complexity, "method_complexity", max_method.name),
        ("Max class WMC", max_class.wmc if max_class else 0, "wmc", max_class.name if max_class else "-"),
        ("Max class CBO", max_cbo.cbo if max_cbo else 0, "cbo", max_cbo.name if max_cbo else "-"),
        ("Max DIT", max_dit.dit if max_dit else 0, "dit", max_dit.name if max_dit else "-"),
        ("Duplication percent", round(duplication, 2), "duplication_percent", "duplicated blocks"),
        ("Max methods per class", len(max_methods.methods) if max_methods else 0, "class_methods", max_methods.name if max_methods else "-"),
    ]

    failures = [item for item in checks if status(float(item[1]), item[2]) == "FAIL"]
    warnings = [item for item in checks if status(float(item[1]), item[2]) == "WARN"]

    summary = {
        "classes": len(class_metrics),
        "methods": len(methods),
        "sloc": sum(1 for line in normalized_code_lines(list((root / "backend/src/main/java").rglob("*.java")))),
        "duplication_percent": round(duplication, 2),
        "failures": len(failures),
        "warnings": len(warnings),
    }

    rows = []
    for label, value, key, owner in checks:
        threshold = THRESHOLDS[key]
        rows.append(
            f"| {label} | {value} | warn > {threshold['warn']} | fail > {threshold['fail']} | "
            f"{status(float(value), key)} | {owner} |"
        )

    top_complex = sorted(
        ((cls, method) for cls in class_metrics for method in cls.methods),
        key=lambda item: item[1].complexity,
        reverse=True,
    )[:10]
    top_rows = [
        f"| {cls.file}:{method.start_line} | {cls.name}.{method.name} | {method.complexity} |"
        for cls, method in top_complex
    ]

    report = "\n".join([
        "# Sentimentum Quality Gate",
        "",
        "Static metrics based on the project quality-metrics lecture: cyclomatic complexity, WMC, CBO, DIT and duplication.",
        "",
        "## Summary",
        "",
        f"- Classes: {summary['classes']}",
        f"- Methods: {summary['methods']}",
        f"- Normalized SLOC: {summary['sloc']}",
        f"- Duplication: {summary['duplication_percent']}%",
        f"- Stopper failures: {summary['failures']}",
        f"- Warnings: {summary['warnings']}",
        "",
        "## Gates",
        "",
        "| Metric | Value | Warning | Stopper | Status | Location |",
        "|---|---:|---:|---:|---|---|",
        *rows,
        "",
        "## Most Complex Methods",
        "",
        "| File | Method | Complexity |",
        "|---|---|---:|",
        *(top_rows or ["| - | - | 0 |"]),
        "",
    ])
    return {"summary": summary, "checks": checks}, report


def main() -> int:
    parser = argparse.ArgumentParser(description="Sentimentum static quality gate")
    parser.add_argument("--root", default=".", help="Repository root")
    parser.add_argument("--report", default="build/reports/quality-gate.md", help="Markdown report path")
    parser.add_argument("--json", default="build/reports/quality-gate.json", help="JSON report path")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    source_root = root / "backend/src/main/java"
    files = sorted(source_root.rglob("*.java"))
    class_metrics = analyze_classes(files, root)
    duplication = duplication_percent(files)
    data, report = build_report(root, class_metrics, duplication)

    report_path = root / args.report
    json_path = root / args.json
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(report, encoding="utf-8")
    json_path.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")

    print(report)
    return 1 if data["summary"]["failures"] else 0


if __name__ == "__main__":
    sys.exit(main())
