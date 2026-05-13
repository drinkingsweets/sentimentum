import {
  BarChart3,
  ChevronRight,
  Database,
  FileText,
  LayoutDashboard,
  Loader2,
  LogOut,
  Mail,
  Phone,
  Plus,
  RefreshCcw,
  Search,
  Settings,
  UserRound
} from "lucide-react";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { api, setApiCredentials } from "./api";
import logoUrl from "./assets/logo.png";
import type {
  AnalysisResult,
  DataSource,
  DataSourceType,
  Message,
  Project,
  Report,
  Sentiment,
  SentimentStats,
  User
} from "./types";

type View = "landing" | "auth" | "projects" | "project" | "profile";
type AuthMode = "login" | "signup";

type CsvImportRow = {
  author: string;
  content: string;
  language: string;
  tag: string;
  sentiment: Sentiment;
  confidence: number;
  createdAt?: string;
};

type AuthSession = {
  email: string;
  password: string;
  user: User;
};

const sentimentLabels: Record<Sentiment, string> = {
  POSITIVE: "Позитив",
  NEUTRAL: "Нейтрально",
  NEGATIVE: "Негатив",
  AMBIGUOUS: "Смешано"
};

const sentimentColors: Record<Sentiment, string> = {
  POSITIVE: "#16a34a",
  NEUTRAL: "#2563eb",
  NEGATIVE: "#ef4444",
  AMBIGUOUS: "#f97316"
};

const sourceTypeLabels: Record<DataSourceType, string> = {
  YOUTUBE: "YouTube",
  VK: "VK",
  TELEGRAM: "Telegram",
  CSV: "CSV",
  LINK: "Ссылка",
  OTHER: "Другое"
};

const dateTime = new Intl.DateTimeFormat("ru-RU", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit"
});

function formatDate(value?: string | null) {
  if (!value) return "—";
  return dateTime.format(new Date(value));
}

function readStoredSession(): AuthSession | null {
  const raw = localStorage.getItem("sentimentum:auth");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthSession;
  } catch {
    localStorage.removeItem("sentimentum:auth");
    return null;
  }
}

export function App() {
  const [view, setView] = useState<View>("landing");
  const [authMode, setAuthMode] = useState<AuthMode>("login");
  const [authSession, setAuthSession] = useState<AuthSession | null>(() => readStoredSession());
  const [users, setUsers] = useState<User[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [sources, setSources] = useState<DataSource[]>([]);
  const [messages, setMessages] = useState<Message[]>([]);
  const [results, setResults] = useState<AnalysisResult[]>([]);
  const [stats, setStats] = useState<SentimentStats[]>([]);
  const [reports, setReports] = useState<Report[]>([]);
  const [activeProjectId, setActiveProjectId] = useState<string>("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const activeUser = authSession?.user;
  const activeProject = projects.find((project) => project.id === activeProjectId) ?? projects[0];
  const projectSources = useMemo(
    () => sources.filter((source) => source.projectId === activeProject?.id),
    [activeProject?.id, sources]
  );
  const projectSourceIds = useMemo(() => new Set(projectSources.map((source) => source.id)), [projectSources]);
  const projectMessages = useMemo(
    () => messages.filter((message) => projectSourceIds.has(message.sourceId)),
    [messages, projectSourceIds]
  );
  const resultByMessage = useMemo(() => {
    const map = new Map<string, AnalysisResult>();
    results.forEach((result) => map.set(result.messageId, result));
    return map;
  }, [results]);

  const totalStats = stats.reduce((sum, item) => sum + item.count, 0);
  const sentimentIndex = useMemo(() => {
    if (!totalStats) return 0;
    const score = stats.reduce((sum, item) => {
      if (item.sentiment === "POSITIVE") return sum + item.count;
      if (item.sentiment === "NEGATIVE") return sum - item.count;
      return sum;
    }, 0);
    return Math.round(((score / totalStats + 1) / 2) * 10 * 10) / 10;
  }, [stats, totalStats]);

  async function loadAll(projectId = activeProject?.id, silent = false) {
    if (!authSession) {
      setUsers([]);
      setProjects([]);
      setSources([]);
      setMessages([]);
      setResults([]);
      setStats([]);
      setReports([]);
      return;
    }
    setIsLoading(true);
    if (!silent) setError("");
    try {
      const [nextUsers, nextProjects, nextSources, nextMessages, nextResults, nextStats, nextReports] =
        await Promise.all([
          api.users.list(),
          api.projects.list(),
          api.dataSources.list(),
          api.messages.list(),
          api.results.list(),
          api.analytics.sentimentStats(projectId),
          api.reports.list(projectId)
        ]);
      setUsers(nextUsers);
      setProjects(nextProjects);
      setSources(nextSources);
      setMessages(nextMessages);
      setResults(nextResults);
      setStats(nextStats);
      setReports(nextReports);
      if (!activeProjectId && nextProjects[0]) setActiveProjectId(nextProjects[0].id);
    } catch (caught) {
      if (!silent) setError(caught instanceof Error ? caught.message : "Не удалось загрузить данные");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    setApiCredentials(authSession ? { email: authSession.email, password: authSession.password } : null);
    if (authSession) {
      localStorage.setItem("sentimentum:auth", JSON.stringify(authSession));
      setUsers([authSession.user]);
      void loadAll(undefined, true);
    } else {
      localStorage.removeItem("sentimentum:auth");
      void loadAll(undefined, true);
    }
  }, [authSession]);

  useEffect(() => {
    if (authSession && activeProject?.id) {
      void api.analytics.sentimentStats(activeProject.id).then(setStats).catch(() => undefined);
      void api.reports.list(activeProject.id).then(setReports).catch(() => undefined);
    }
  }, [activeProject?.id, authSession?.email]);

  function showNotice(value: string) {
    setNotice(value);
    window.setTimeout(() => setNotice(""), 2800);
  }

  async function handleLogin(email: string, password: string) {
    setIsLoading(true);
    setError("");
    try {
      setApiCredentials({ email, password });
      const user = await api.users.me();
      setAuthSession({ email, password, user });
      setUsers([user]);
      setView("projects");
      showNotice("Вход выполнен");
    } catch (caught) {
      setApiCredentials(null);
      setError(caught instanceof Error ? caught.message : "Не удалось войти");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleSignup(name: string, email: string, password: string) {
    setIsLoading(true);
    setError("");
    try {
      const user = await api.users.create({ name, email, password });
      setApiCredentials({ email, password });
      setAuthSession({ email, password, user });
      setUsers([user]);
      setView("projects");
      showNotice("Аккаунт создан");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Не удалось создать пользователя");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCreateProject(payload: { name: string; description: string }) {
    if (!activeUser) {
      setError("Сначала создайте пользователя");
      setView("auth");
      return;
    }
    setIsLoading(true);
    try {
      const project = await api.projects.create(payload);
      setProjects((current) => [project, ...current]);
      setActiveProjectId(project.id);
      setView("project");
      showNotice("Проект создан");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Не удалось создать проект");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCreateSource(payload: { name: string; link: string; type: DataSourceType }) {
    if (!activeProject) return;
    const source = await api.dataSources.create({ ...payload, projectId: activeProject.id });
    setSources((current) => [source, ...current]);
    showNotice("Источник добавлен");
    return source;
  }

  async function handleImportCsvSource(payload: { name: string; link: string; type: "CSV" }, rows: CsvImportRow[]) {
    if (!activeProject) return;
    setIsLoading(true);
    setError("");
    try {
      const source = await api.dataSources.create({ ...payload, projectId: activeProject.id });
      const importedMessages: Message[] = [];
      const importedResults: AnalysisResult[] = [];

      for (const row of rows) {
        const message = await api.messages.create({
          sourceId: source.id,
          content: row.content,
          author: row.author,
          language: row.language,
          tag: row.tag,
          createdAt: row.createdAt
        });
        const result = await api.results.create({
          messageId: message.id,
          sentiment: row.sentiment,
          confidence: row.confidence
        });
        importedMessages.push(message);
        importedResults.push(result);
      }

      setSources((current) => [source, ...current]);
      setMessages((current) => [...importedMessages, ...current]);
      setResults((current) => [...importedResults, ...current]);
      setStats(await api.analytics.sentimentStats(activeProject.id));
      showNotice(`CSV загружен: ${rows.length} комментариев`);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Не удалось загрузить CSV");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleImportYouTubeComments(payload: { video: string; maxResults: number }) {
    if (!activeProject) return;
    setIsLoading(true);
    setError("");
    try {
      const response = await api.youtube.importComments({
        projectId: activeProject.id,
        video: payload.video,
        maxResults: payload.maxResults
      });
      await loadAll(activeProject.id, true);
      showNotice(`YouTube импортирован: ${response.importedCount} комментариев`);
    } catch (caught) {
      const message = caught instanceof Error ? caught.message : "Не удалось импортировать комментарии YouTube";
      setError(message);
      throw new Error(message);
    } finally {
      setIsLoading(false);
    }
  }

  async function handleCreateMessage(payload: {
    sourceId: string;
    content: string;
    author: string;
    language: string;
    tag: string;
    sentiment: Sentiment;
    confidence: number;
  }) {
    const message = await api.messages.create({
      sourceId: payload.sourceId,
      content: payload.content,
      author: payload.author,
      language: payload.language,
      tag: payload.tag,
      createdAt: new Date().toISOString()
    });
    const result = await api.results.create({
      messageId: message.id,
      sentiment: payload.sentiment,
      confidence: payload.confidence
    });
    setMessages((current) => [message, ...current]);
    setResults((current) => [result, ...current]);
    if (activeProject?.id) setStats(await api.analytics.sentimentStats(activeProject.id));
    showNotice("Сообщение и результат анализа сохранены");
  }

  async function handleCreateReport(title: string) {
    if (!activeProject) return;
    const payload = {
      project: activeProject.name,
      generatedAt: new Date().toISOString(),
      sentimentIndex,
      stats,
      sources: projectSources.length,
      messages: projectMessages.length
    };
    const report = await api.reports.create({
      projectId: activeProject.id,
      title,
      format: "JSON",
      data: JSON.stringify(payload, null, 2)
    });
    setReports((current) => [report, ...current]);
    showNotice("JSON-отчет создан");
  }

  const shell = view !== "landing" && view !== "auth";

  return (
    <div className="app">
      {shell && (
        <Sidebar
          active={view}
          onNavigate={setView}
          user={activeUser}
          onLogout={() => {
            setAuthSession(null);
            setActiveProjectId("");
            setView("landing");
          }}
        />
      )}

      <main className={shell ? "workspace" : "public-page"}>
        {error && <Toast tone="error" text={error} onClose={() => setError("")} />}
        {notice && <Toast tone="success" text={notice} onClose={() => setNotice("")} />}

        {view === "landing" && (
          <Landing
            onLogin={() => {
              setAuthMode("login");
              setView("auth");
            }}
            onSignup={() => {
              setAuthMode("signup");
              setView("auth");
            }}
          />
        )}

        {view === "auth" && (
          <AuthPanel
            mode={authMode}
            users={users}
            isLoading={isLoading}
            onMode={setAuthMode}
            onLogin={handleLogin}
            onSignup={handleSignup}
            onBack={() => setView("landing")}
          />
        )}

        {view === "projects" && (
          <ProjectsView
            isLoading={isLoading}
            projects={projects}
            users={users}
            activeUser={activeUser}
            onRefresh={() => loadAll(activeProject?.id)}
            onCreateProject={handleCreateProject}
            onOpenProject={(id) => {
              setActiveProjectId(id);
              setView("project");
            }}
          />
        )}

        {view === "project" && (
          <ProjectView
            project={activeProject}
            projects={projects}
            sources={projectSources}
            messages={projectMessages}
            reports={reports}
            stats={stats}
            resultByMessage={resultByMessage}
            sentimentIndex={sentimentIndex}
            isLoading={isLoading}
            onSelectProject={setActiveProjectId}
            onRefresh={() => loadAll(activeProject?.id)}
            onCreateSource={handleCreateSource}
            onImportCsvSource={handleImportCsvSource}
            onImportYouTubeComments={handleImportYouTubeComments}
            onCreateMessage={handleCreateMessage}
            onCreateReport={handleCreateReport}
          />
        )}

        {view === "profile" && (
          <ProfileView
            user={activeUser}
            projects={projects}
          />
        )}
      </main>
    </div>
  );
}

function Toast({ tone, text, onClose }: { tone: "error" | "success"; text: string; onClose: () => void }) {
  return (
    <button className={`toast ${tone}`} onClick={onClose} type="button">
      {text}
    </button>
  );
}

function Landing({ onLogin, onSignup }: { onLogin: () => void; onSignup: () => void }) {
  return (
    <section className="landing">
      <header className="landing-nav">
        <div className="brand">
          <span className="brand-mark">
            <img src={logoUrl} alt="sentimentum" />
          </span>
        </div>
        <nav>
          <button type="button" onClick={onLogin}>
            Вход
          </button>
          <button className="primary-link" type="button" onClick={onSignup}>
            Регистрация
          </button>
        </nav>
      </header>

      <div className="hero">
        <h1>sentimentum</h1>
        <div className="hero-copy">
          <p>Интеллектуальная классификация сообщений по тональности — ваш инструмент для объективной оценки контента, брендов и событий</p>
          <button className="button primary" type="button" onClick={onSignup}>
            Попробовать
          </button>
        </div>
        <div className="hero-chart" aria-hidden="true">
          <div className="chart-back" />
          <div className="chart-card">
            <svg viewBox="0 0 420 260" role="img" aria-label="График тональности">
              <defs>
                <linearGradient id="sentimentGradient" x1="100" y1="168" x2="370" y2="24" gradientUnits="userSpaceOnUse">
                  <stop stopColor="#2563EB" />
                  <stop offset="0.52" stopColor="#7C6DA5" />
                  <stop offset="1" stopColor="#F97316" />
                </linearGradient>
              </defs>
              <path className="axis" d="M54 210H364" />
              <path className="axis" d="M64 220V48" />
              <path className="axis-arrow" d="M359 205l6 5-6 5" />
              <path className="axis-arrow" d="M59 53l5-6 5 6" />
              <path className="sentiment-line" d="M100 168L154 88L216 118L306 84L370 24" />
            </svg>
          </div>
        </div>
      </div>

      <div className="feature-strip">
        <Feature title="Предобработка текста" text="" />
        <Feature title="Гибкий сбор данных" text="" />
        <Feature title="Мультиязычная поддержка" text="" />
      </div>

      <footer className="landing-footer">
        <strong>Связаться с нами</strong>
        <div className="contact-card">
          <span>
            <Phone size={24} />
            +7-001-002-03-04
          </span>
          <span>
            <Mail size={25} />
            support@mail.ru
          </span>
        </div>
        <span className="copyright">© 2025 <strong>sentimentum</strong>Company</span>
      </footer>
    </section>
  );
}

function Feature({ title, text }: { title: string; text: string }) {
  return (
    <article className="feature">
      <h2>{title}</h2>
      <p>{text}</p>
    </article>
  );
}

function splitCsvLine(line: string) {
  const cells: string[] = [];
  let current = "";
  let quoted = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    const next = line[index + 1];

    if (char === '"' && quoted && next === '"') {
      current += '"';
      index += 1;
      continue;
    }

    if (char === '"') {
      quoted = !quoted;
      continue;
    }

    if (char === "," && !quoted) {
      cells.push(current.trim());
      current = "";
      continue;
    }

    current += char;
  }

  cells.push(current.trim());
  return cells;
}

function parseCommentsCsv(text: string): CsvImportRow[] {
  const lines = text
    .replace(/^\uFEFF/, "")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);

  if (lines.length < 2) {
    throw new Error("CSV должен содержать заголовок и хотя бы одну строку");
  }

  const headers = splitCsvLine(lines[0]).map((header) => header.toLowerCase());
  const indexOf = (name: string) => headers.indexOf(name.toLowerCase());
  const contentIndex = indexOf("content");

  if (contentIndex < 0) {
    throw new Error("В CSV обязательна колонка content");
  }

  return lines.slice(1).map((line, lineIndex) => {
    const cells = splitCsvLine(line);
    const value = (name: string) => {
      const index = indexOf(name);
      return index >= 0 ? cells[index]?.trim() ?? "" : "";
    };
    const rawSentiment = value("sentiment").toUpperCase();
    const sentiment = ["POSITIVE", "NEUTRAL", "NEGATIVE", "AMBIGUOUS"].includes(rawSentiment)
      ? (rawSentiment as Sentiment)
      : "NEUTRAL";
    const confidence = Number(value("confidence") || 0.7);
    const content = cells[contentIndex]?.trim();

    if (!content) {
      throw new Error(`В CSV пустой content в строке ${lineIndex + 2}`);
    }

    return {
      author: value("author") || "csv_user",
      content,
      language: value("language") || "ru",
      tag: value("tag") || "csv",
      sentiment,
      confidence: Number.isFinite(confidence) ? Math.min(Math.max(confidence, 0), 1) : 0.7,
      createdAt: value("createdAt") || undefined
    };
  });
}

function AuthPanel({
  mode,
  users,
  isLoading,
  onMode,
  onLogin,
  onSignup,
  onBack
}: {
  mode: AuthMode;
  users: User[];
  isLoading: boolean;
  onMode: (mode: AuthMode) => void;
  onLogin: (email: string, password: string) => void;
  onSignup: (name: string, email: string, password: string) => void;
  onBack: () => void;
}) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    if (mode === "login") {
      void onLogin(email || users[0]?.email || "", password);
      return;
    }
    void onSignup(name, email, password);
  }

  return (
    <section className="auth-layout">
      <div className="auth-card">
        <button className="plain-action" type="button" onClick={onBack}>
          sentimentum
        </button>
        <h1>{mode === "login" ? "Вход" : "Регистрация"}</h1>
        <form onSubmit={submit}>
          {mode === "signup" && (
            <label>
              Имя
              <input value={name} onChange={(event) => setName(event.target.value)} required placeholder="Иван Иванов" />
            </label>
          )}
          <label>
            Почта
            <input
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              placeholder={users[0]?.email ?? "email@edu.fa.ru"}
            />
          </label>
          <label>
            Пароль
            <input
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              placeholder="Пароль"
            />
          </label>
          <button className="button primary full" disabled={isLoading} type="submit">
            {isLoading && <Loader2 className="spin" size={18} />}
            {mode === "login" ? "Войти" : "Зарегистрироваться"}
          </button>
        </form>
        <button className="plain-action centered" type="button" onClick={() => onMode(mode === "login" ? "signup" : "login")}>
          {mode === "login" ? "Нет аккаунта? Зарегистрируйтесь" : "Уже есть аккаунт? Войти"}
        </button>
      </div>
      <div className="auth-illustration">
        <h2>Добро пожаловать!</h2>
      </div>
    </section>
  );
}

function Sidebar({
  active,
  user,
  onNavigate,
  onLogout
}: {
  active: View;
  user?: User;
  onNavigate: (view: View) => void;
  onLogout: () => void;
}) {
  return (
    <aside className="sidebar">
      <div className="brand compact">
        <span className="brand-mark">
          <img src={logoUrl} alt="sentimentum" />
        </span>
        sentimentum
      </div>
      <nav className="side-nav">
        <button className={active === "projects" ? "active" : ""} type="button" onClick={() => onNavigate("projects")}>
          <LayoutDashboard size={20} />
          Мои проекты
        </button>
        <button className={active === "project" ? "active" : ""} type="button" onClick={() => onNavigate("project")}>
          <BarChart3 size={20} />
          Аналитика
        </button>
        <button className={active === "profile" ? "active" : ""} type="button" onClick={() => onNavigate("profile")}>
          <UserRound size={20} />
          Профиль
        </button>
      </nav>
      <div className="side-footer">
        <div>
          <strong>{user?.name ?? "Гость"}</strong>
          <span>{user?.email ?? "Создайте аккаунт"}</span>
        </div>
        <button className="icon-button" type="button" onClick={onLogout} aria-label="Выйти" title="Выйти">
          <LogOut size={19} />
        </button>
      </div>
    </aside>
  );
}

function ProjectsView({
  projects,
  users,
  activeUser,
  isLoading,
  onRefresh,
  onCreateProject,
  onOpenProject
}: {
  projects: Project[];
  users: User[];
  activeUser?: User;
  isLoading: boolean;
  onRefresh: () => void;
  onCreateProject: (payload: { name: string; description: string }) => void;
  onOpenProject: (id: string) => void;
}) {
  const [query, setQuery] = useState("");
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const filtered = projects.filter((project) => `${project.name} ${project.description ?? ""}`.toLowerCase().includes(query.toLowerCase()));

  function submit(event: FormEvent) {
    event.preventDefault();
    void onCreateProject({ name, description });
    setName("");
    setDescription("");
  }

  return (
    <section className="screen">
      <ScreenHeader
        title="Проекты"
        subtitle={activeUser ? `${activeUser.name}, ${projects.length} проектов в системе` : "Создайте пользователя для добавления проектов"}
        action={<RefreshButton isLoading={isLoading} onClick={onRefresh} />}
      />

      <div className="toolbar">
        <div className="search-box">
          <Search size={18} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Поиск" />
        </div>
        <span>{users.length} пользователей</span>
      </div>

      <form className="inline-form" onSubmit={submit}>
        <label>
          Название
          <input value={name} onChange={(event) => setName(event.target.value)} required placeholder="Видео 1" />
        </label>
        <label>
          Описание
          <input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="Кампания, продукт или событие" />
        </label>
        <button className="button primary" type="submit">
          <Plus size={18} />
          Добавить
        </button>
      </form>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th># ID</th>
              <th>Название</th>
              <th>Владелец</th>
              <th>Создан</th>
              <th>Обновлен</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {filtered.map((project, index) => (
              <tr key={project.id}>
                <td>#{String(index + 1).padStart(4, "0")}</td>
                <td>
                  <strong>{project.name}</strong>
                  <span>{project.description || "Без описания"}</span>
                </td>
                <td>{users.find((user) => user.id === project.ownerId)?.name ?? "—"}</td>
                <td>{formatDate(project.createdAt)}</td>
                <td>{formatDate(project.updatedAt)}</td>
                <td>
                  <button className="table-action" type="button" onClick={() => onOpenProject(project.id)}>
                    Открыть
                    <ChevronRight size={17} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!filtered.length && <EmptyState title="Проектов пока нет" text="Добавьте первый проект, чтобы подключить источники и аналитику." />}
      </div>
    </section>
  );
}

function ProjectView({
  project,
  projects,
  sources,
  messages,
  stats,
  reports,
  resultByMessage,
  sentimentIndex,
  isLoading,
  onSelectProject,
  onRefresh,
  onCreateSource,
  onImportCsvSource,
  onImportYouTubeComments,
  onCreateMessage,
  onCreateReport
}: {
  project?: Project;
  projects: Project[];
  sources: DataSource[];
  messages: Message[];
  stats: SentimentStats[];
  reports: Report[];
  resultByMessage: Map<string, AnalysisResult>;
  sentimentIndex: number;
  isLoading: boolean;
  onSelectProject: (id: string) => void;
  onRefresh: () => void;
  onCreateSource: (payload: { name: string; link: string; type: DataSourceType }) => Promise<DataSource | undefined>;
  onImportCsvSource: (payload: { name: string; link: string; type: "CSV" }, rows: CsvImportRow[]) => Promise<void>;
  onImportYouTubeComments: (payload: { video: string; maxResults: number }) => Promise<void>;
  onCreateMessage: (payload: {
    sourceId: string;
    content: string;
    author: string;
    language: string;
    tag: string;
    sentiment: Sentiment;
    confidence: number;
  }) => Promise<void>;
  onCreateReport: (title: string) => Promise<void>;
}) {
  const [sourceName, setSourceName] = useState("");
  const [sourceLink, setSourceLink] = useState("");
  const [sourceType, setSourceType] = useState<DataSourceType>("YOUTUBE");
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [content, setContent] = useState("");
  const [author, setAuthor] = useState("");
  const [language, setLanguage] = useState("ru");
  const [tag, setTag] = useState("общее");
  const [sentiment, setSentiment] = useState<Sentiment>("NEUTRAL");
  const [confidence, setConfidence] = useState(0.82);
  const [sourceId, setSourceId] = useState("");
  const [reportTitle, setReportTitle] = useState("Сводка тональности");
  const [sourceFormError, setSourceFormError] = useState("");
  const [sourceFormInfo, setSourceFormInfo] = useState("");
  const [youtubeMaxResults, setYoutubeMaxResults] = useState(50);

  useEffect(() => {
    if (!sourceId && sources[0]) setSourceId(sources[0].id);
    if (sourceId && !sources.some((source) => source.id === sourceId)) setSourceId(sources[0]?.id ?? "");
  }, [sourceId, sources]);

  if (!project) {
    return (
      <section className="screen">
        <EmptyState title="Нет выбранного проекта" text="Создайте проект на вкладке «Мои проекты»." />
      </section>
    );
  }

  async function submitSource(event: FormEvent) {
    event.preventDefault();
    setSourceFormError("");
    setSourceFormInfo("");
    try {
      if (sourceType === "CSV") {
        if (!csvFile) {
          throw new Error("Выберите CSV-файл");
        }
        const rows = parseCommentsCsv(await csvFile.text());
        await onImportCsvSource(
          {
            name: sourceName || csvFile.name,
            link: `file:${csvFile.name}`,
            type: "CSV"
          },
          rows
        );
        setCsvFile(null);
      } else if (sourceType === "YOUTUBE") {
        await onImportYouTubeComments({ video: sourceLink, maxResults: youtubeMaxResults });
      } else {
        await onCreateSource({ name: sourceName, link: sourceLink, type: sourceType });
        setSourceFormInfo("Источник создан. Автоматический импорт комментариев по ссылке пока не поддерживается backend; используйте CSV или ручное добавление сообщений.");
      }
      setSourceName("");
      setSourceLink("");
    } catch (caught) {
      setSourceFormError(caught instanceof Error ? caught.message : "Не удалось загрузить источник");
    }
  }

  async function submitMessage(event: FormEvent) {
    event.preventDefault();
    await onCreateMessage({ sourceId, content, author, language, tag, sentiment, confidence });
    setContent("");
    setAuthor("");
  }

  async function submitReport(event: FormEvent) {
    event.preventDefault();
    await onCreateReport(reportTitle);
  }

  const topTags = Object.entries(
    messages.reduce<Record<string, number>>((acc, message) => {
      acc[message.tag] = (acc[message.tag] ?? 0) + 1;
      return acc;
    }, {})
  )
    .sort((a, b) => b[1] - a[1])
    .slice(0, 7);
  const sourceTypeStats = Object.entries(
    sources.reduce<Record<string, number>>((acc, source) => {
      acc[source.type] = (acc[source.type] ?? 0) + 1;
      return acc;
    }, {})
  ).sort((a, b) => b[1] - a[1]);
  const sourceMessageStats = sources
    .map((source) => ({
      name: source.name,
      count: messages.filter((message) => message.sourceId === source.id).length
    }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 5);
  const totalSentiments = stats.reduce((sum, item) => sum + item.count, 0);
  let donutOffset = 25;

  return (
    <section className="screen">
      <ScreenHeader
        title={project.name}
        subtitle={project.description ?? "Аналитика тональности по подключенным источникам"}
        action={
          <div className="header-actions">
            <select value={project.id} onChange={(event) => onSelectProject(event.target.value)}>
              {projects.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
            <RefreshButton isLoading={isLoading} onClick={onRefresh} />
          </div>
        }
      />

      <div className="metrics">
        <Metric icon={<Database size={21} />} label="Источники данных" value={sources.length} />
        <Metric icon={<FileText size={21} />} label="Всего объектов" value={messages.length} />
        <Metric icon={<BarChart3 size={21} />} label="Индекс настроения" value={sentimentIndex || "—"} accent />
        <Metric icon={<Settings size={21} />} label="Отчеты" value={reports.length} />
      </div>

      <div className="analytics-grid">
        <section className="panel">
          <h2>Тональность</h2>
          <div className="bars">
            {stats.map((item) => {
              const total = stats.reduce((sum, stat) => sum + stat.count, 0) || 1;
              const width = Math.max((item.count / total) * 100, item.count ? 8 : 0);
              return (
                <div className="bar-row" key={item.sentiment}>
                  <span>{sentimentLabels[item.sentiment]}</span>
                  <div className="bar-track">
                    <div style={{ width: `${width}%`, backgroundColor: sentimentColors[item.sentiment] }} />
                  </div>
                  <strong>{item.count}</strong>
                </div>
              );
            })}
          </div>
        </section>

        <section className="panel">
          <h2>Топ ключевых тем по тегу</h2>
          <div className="tag-cloud">
            {topTags.map(([name, count]) => (
              <span key={name}>
                {name}
                <strong>{count}</strong>
              </span>
            ))}
            {!topTags.length && <p className="muted">Пока нет сообщений для расчета тем.</p>}
          </div>
        </section>
      </div>

      <div className="charts-grid">
        <section className="panel chart-panel">
          <h2>Диаграмма тональности</h2>
          <div className="donut-layout">
            <svg className="donut" viewBox="0 0 42 42" role="img" aria-label="Распределение тональности">
              <circle className="donut-bg" cx="21" cy="21" r="15.9" />
              {stats.map((item) => {
                const percent = totalSentiments ? (item.count / totalSentiments) * 100 : 0;
                const dash = `${percent} ${100 - percent}`;
                const offset = donutOffset;
                donutOffset -= percent;
                return (
                  <circle
                    className="donut-segment"
                    cx="21"
                    cy="21"
                    key={item.sentiment}
                    r="15.9"
                    stroke={sentimentColors[item.sentiment]}
                    strokeDasharray={dash}
                    strokeDashoffset={offset}
                  />
                );
              })}
              <text x="21" y="20.2" textAnchor="middle">
                {totalSentiments}
              </text>
              <text className="donut-caption" x="21" y="25" textAnchor="middle">
                всего
              </text>
            </svg>
            <div className="chart-legend">
              {stats.map((item) => (
                <span key={item.sentiment}>
                  <i style={{ backgroundColor: sentimentColors[item.sentiment] }} />
                  {sentimentLabels[item.sentiment]}
                  <strong>{item.count}</strong>
                </span>
              ))}
            </div>
          </div>
        </section>

        <section className="panel chart-panel">
          <h2>Источники и объем данных</h2>
          <div className="source-bars">
            {sourceMessageStats.map((item) => {
              const max = Math.max(...sourceMessageStats.map((entry) => entry.count), 1);
              return (
                <div className="source-bar-row" key={item.name}>
                  <span>{item.name}</span>
                  <div>
                    <i style={{ width: `${Math.max((item.count / max) * 100, item.count ? 8 : 0)}%` }} />
                  </div>
                  <strong>{item.count}</strong>
                </div>
              );
            })}
            {!sourceMessageStats.length && <p className="muted">Добавьте CSV или сообщения, чтобы увидеть объем по источникам.</p>}
          </div>
          <div className="source-type-list">
            {sourceTypeStats.map(([type, count]) => (
              <span key={type}>
                {sourceTypeLabels[type as DataSourceType] ?? type}
                <strong>{count}</strong>
              </span>
            ))}
          </div>
        </section>
      </div>

      <div className="forms-grid">
        <form className="panel form-panel" onSubmit={submitSource}>
          <h2>Добавить источник</h2>
          <label>
            Название
            <input
              value={sourceName}
              onChange={(event) => setSourceName(event.target.value)}
              required={sourceType !== "CSV" && sourceType !== "YOUTUBE"}
              placeholder={sourceType === "CSV" ? "Имя источника или название файла" : sourceType === "YOUTUBE" ? "Название будет создано автоматически" : "Источник комментариев"}
            />
          </label>
          <label>
            Тип
            <select
              value={sourceType}
              onChange={(event) => {
                setSourceType(event.target.value as DataSourceType);
                setSourceLink("");
                setCsvFile(null);
              }}
            >
              {Object.entries(sourceTypeLabels).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </label>
          {sourceType === "CSV" ? (
            <label>
              CSV-файл
              <input
                accept=".csv,text/csv"
                type="file"
                required
                onChange={(event) => setCsvFile(event.target.files?.[0] ?? null)}
              />
              <span className="field-hint">Нужна колонка content. Дополнительно: author, language, tag, sentiment, confidence, createdAt.</span>
            </label>
          ) : (
            <>
              <label>
                Ссылка
                <input value={sourceLink} onChange={(event) => setSourceLink(event.target.value)} required placeholder="https://..." />
                <span className="field-hint">
                  {sourceType === "YOUTUBE"
                    ? "Комментарии импортируются через backend YouTube Data API. Для работы нужен YOUTUBE_API_KEY в compose."
                    : "Ссылка сохраняется как источник. Автоимпорт для этого типа пока не поддерживается backend."}
                </span>
              </label>
              {sourceType === "YOUTUBE" && (
                <label>
                  Количество комментариев
                  <input
                    max={100}
                    min={1}
                    onChange={(event) => setYoutubeMaxResults(Number(event.target.value))}
                    required
                    type="number"
                    value={youtubeMaxResults}
                  />
                </label>
              )}
            </>
          )}
          <button className="button primary" type="submit">
            <Plus size={18} />
            {sourceType === "CSV" ? "Загрузить CSV" : sourceType === "YOUTUBE" ? "Импортировать YouTube" : "Сохранить источник"}
          </button>
          {sourceFormInfo && <p className="form-info">{sourceFormInfo}</p>}
          {sourceFormError && <p className="form-error">{sourceFormError}</p>}
        </form>

        <form className="panel form-panel" onSubmit={submitMessage}>
          <h2>Добавить сообщение</h2>
          <label>
            Источник
            <select value={sourceId} onChange={(event) => setSourceId(event.target.value)} required>
              <option value="" disabled>
                Выберите источник
              </option>
              {sources.map((source) => (
                <option key={source.id} value={source.id}>
                  {source.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Текст
            <textarea value={content} onChange={(event) => setContent(event.target.value)} required placeholder="Комментарий пользователя" />
          </label>
          <div className="split">
            <label>
              Автор
              <input value={author} onChange={(event) => setAuthor(event.target.value)} placeholder="username" />
            </label>
            <label>
              Язык
              <input value={language} onChange={(event) => setLanguage(event.target.value)} required />
            </label>
          </div>
          <div className="split">
            <label>
              Тег
              <input value={tag} onChange={(event) => setTag(event.target.value)} required />
            </label>
            <label>
              Тональность
              <select value={sentiment} onChange={(event) => setSentiment(event.target.value as Sentiment)}>
                {Object.entries(sentimentLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <label>
            Уверенность: {Math.round(confidence * 100)}%
            <input
              type="range"
              min="0"
              max="1"
              step="0.01"
              value={confidence}
              onChange={(event) => setConfidence(Number(event.target.value))}
            />
          </label>
          <button className="button primary" type="submit" disabled={!sources.length}>
            <Plus size={18} />
            Сохранить сообщение
          </button>
        </form>

        <form className="panel form-panel" onSubmit={submitReport}>
          <h2>Сформировать отчет</h2>
          <label>
            Название
            <input value={reportTitle} onChange={(event) => setReportTitle(event.target.value)} required />
          </label>
          <button className="button secondary" type="submit">
            <FileText size={18} />
            Создать JSON
          </button>
          <div className="report-list">
            {reports.slice(0, 4).map((report) => (
              <span key={report.id}>{report.title}</span>
            ))}
          </div>
        </form>
      </div>

      <div className="table-wrap compact-table">
        <table>
          <thead>
            <tr>
              <th>Сообщение</th>
              <th>Источник</th>
              <th>Тег</th>
              <th>Тональность</th>
              <th>Уверенность</th>
            </tr>
          </thead>
          <tbody>
            {messages.slice(0, 12).map((message) => {
              const result = resultByMessage.get(message.id);
              return (
                <tr key={message.id}>
                  <td>
                    <strong>{message.author || "Аноним"}</strong>
                    <span>{message.content}</span>
                  </td>
                  <td>{sources.find((source) => source.id === message.sourceId)?.name ?? "—"}</td>
                  <td>{message.tag}</td>
                  <td>{result ? sentimentLabels[result.sentiment] : "Не обработано"}</td>
                  <td>{result?.confidence == null ? "—" : `${Math.round(Number(result.confidence) * 100)}%`}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {!messages.length && <EmptyState title="Сообщений пока нет" text="Добавьте источник и несколько сообщений, чтобы увидеть аналитику." />}
      </div>
    </section>
  );
}

function ProfileView({
  user,
  projects
}: {
  user?: User;
  projects: Project[];
}) {
  return (
    <section className="screen">
      <ScreenHeader title="Персональная информация" subtitle="Текущий пользователь для операций с backend API" />
      <div className="profile-grid">
        <section className="panel profile-card">
          <div className="avatar">{user?.name?.slice(0, 1).toUpperCase() ?? "S"}</div>
          <h2>{user?.name ?? "Гость"}</h2>
          <p>{user?.email ?? "Пользователь не выбран"}</p>
          <span>Дата регистрации: {formatDate(user?.createdAt)}</span>
        </section>
        <section className="panel profile-meta">
          <h2>Аккаунт</h2>
          <dl>
            <div>
              <dt>Имя</dt>
              <dd>{user?.name ?? "Не указано"}</dd>
            </div>
            <div>
              <dt>Email</dt>
              <dd>{user?.email ?? "Не указан"}</dd>
            </div>
            <div>
              <dt>ID</dt>
              <dd>{user?.id ?? "—"}</dd>
            </div>
          </dl>
        </section>
      </div>

      <section className="panel">
        <h2>Недавние проекты</h2>
        <div className="project-cards">
          {projects.map((project) => (
            <article key={project.id}>
              <strong>{project.name}</strong>
              <span>{project.description || "Без описания"}</span>
              <small>{formatDate(project.createdAt)}</small>
            </article>
          ))}
          {!projects.length && <p className="muted">У выбранного пользователя пока нет проектов.</p>}
        </div>
      </section>
    </section>
  );
}

function ScreenHeader({
  title,
  subtitle,
  action
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}) {
  return (
    <header className="screen-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {action}
    </header>
  );
}

function RefreshButton({ isLoading, onClick }: { isLoading: boolean; onClick: () => void }) {
  return (
    <button className="icon-button" type="button" onClick={onClick} disabled={isLoading} aria-label="Обновить" title="Обновить">
      <RefreshCcw className={isLoading ? "spin" : ""} size={19} />
    </button>
  );
}

function Metric({ icon, label, value, accent }: { icon: React.ReactNode; label: string; value: string | number; accent?: boolean }) {
  return (
    <article className={`metric ${accent ? "accent" : ""}`}>
      <div>{icon}</div>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return (
    <div className="empty">
      <strong>{title}</strong>
      <span>{text}</span>
    </div>
  );
}
