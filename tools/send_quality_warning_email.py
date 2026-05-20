#!/usr/bin/env python3
import json
import os
import smtplib
import sys
from email.message import EmailMessage
from pathlib import Path


def required_env(names: list[str]) -> dict[str, str] | None:
    values = {name: os.environ.get(name, "").strip() for name in names}
    missing = [name for name, value in values.items() if not value]
    if missing:
        print(f"Quality warning email skipped: missing env vars {', '.join(missing)}")
        return None
    return values


def main() -> int:
    report_json = Path(os.environ.get("QUALITY_GATE_JSON", "build/reports/quality-gate.json"))
    report_md = Path(os.environ.get("QUALITY_GATE_REPORT", "build/reports/quality-gate.md"))

    if not report_json.exists():
        print(f"Quality warning email skipped: {report_json} does not exist")
        return 0

    data = json.loads(report_json.read_text(encoding="utf-8"))
    warnings = int(data.get("summary", {}).get("warnings", 0))
    if warnings == 0:
        print("Quality warning email skipped: no warnings")
        return 0

    env = required_env([
        "SMTP_HOST",
        "SMTP_PORT",
        "SMTP_USERNAME",
        "SMTP_PASSWORD",
        "QUALITY_ALERT_FROM",
        "QUALITY_ALERT_TO",
    ])
    if env is None:
        return 0

    body = report_md.read_text(encoding="utf-8") if report_md.exists() else json.dumps(data, indent=2)
    subject = f"Sentimentum quality gate warning: {warnings} warning(s)"

    message = EmailMessage()
    message["Subject"] = subject
    message["From"] = env["QUALITY_ALERT_FROM"]
    message["To"] = env["QUALITY_ALERT_TO"]
    message.set_content(body)

    with smtplib.SMTP(env["SMTP_HOST"], int(env["SMTP_PORT"])) as smtp:
        smtp.starttls()
        smtp.login(env["SMTP_USERNAME"], env["SMTP_PASSWORD"])
        smtp.send_message(message)

    print(f"Quality warning email sent to {env['QUALITY_ALERT_TO']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
