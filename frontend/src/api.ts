import type {
  AnalysisResult,
  ApiError,
  DataSource,
  DataSourceType,
  Message,
  Project,
  Report,
  ReportFormat,
  Sentiment,
  SentimentStats,
  User,
  UUID
} from "./types";

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "";
type AuthCredentials = { email: string; password: string };

let credentials: AuthCredentials | null = null;

function encodeBasicAuth(auth: AuthCredentials) {
  return `Basic ${window.btoa(`${auth.email}:${auth.password}`)}`;
}

export function setApiCredentials(nextCredentials: AuthCredentials | null) {
  credentials = nextCredentials;
}

async function request<T>(path: string, init?: RequestInit & { skipAuth?: boolean }): Promise<T> {
  const { skipAuth, ...fetchInit } = init ?? {};
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(fetchInit.headers as Record<string, string> | undefined)
  };

  if (credentials && !skipAuth) {
    headers.Authorization = encodeBasicAuth(credentials);
  }

  const response = await fetch(`${API_BASE}${path}`, {
    headers,
    ...fetchInit
  });

  if (!response.ok) {
    let payload: ApiError | undefined;
    try {
      payload = (await response.json()) as ApiError;
    } catch {
      payload = undefined;
    }
    throw new Error(payload?.message ?? `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  users: {
    list: () => request<User[]>("/api/users"),
    me: () => request<User>("/api/users/me"),
    create: (payload: { name: string; email: string; password: string }) =>
      request<User>("/api/users", { method: "POST", body: JSON.stringify(payload), skipAuth: true })
  },
  projects: {
    list: () => request<Project[]>("/api/projects"),
    create: (payload: { name: string; description?: string }) =>
      request<Project>("/api/projects", { method: "POST", body: JSON.stringify(payload) })
  },
  dataSources: {
    list: (projectId?: UUID) =>
      request<DataSource[]>(`/api/data-sources${projectId ? `?projectId=${projectId}` : ""}`),
    create: (payload: { name: string; link: string; type: DataSourceType; projectId: UUID }) =>
      request<DataSource>("/api/data-sources", { method: "POST", body: JSON.stringify(payload) })
  },
  messages: {
    list: (sourceId?: UUID) => request<Message[]>(`/api/messages${sourceId ? `?sourceId=${sourceId}` : ""}`),
    create: (payload: {
      content: string;
      author?: string;
      sourceId: UUID;
      language: string;
      tag: string;
      createdAt?: string;
    }) => request<Message>("/api/messages", { method: "POST", body: JSON.stringify(payload) })
  },
  results: {
    list: (messageId?: UUID) =>
      request<AnalysisResult[]>(`/api/analysis-results${messageId ? `?messageId=${messageId}` : ""}`),
    create: (payload: { messageId: UUID; sentiment: Sentiment; confidence: number }) =>
      request<AnalysisResult>("/api/analysis-results", { method: "POST", body: JSON.stringify(payload) })
  },
  analytics: {
    sentimentStats: (projectId?: UUID) =>
      request<SentimentStats[]>(`/api/analytics/sentiment-stats${projectId ? `?projectId=${projectId}` : ""}`)
  },
  reports: {
    list: (projectId?: UUID) => request<Report[]>(`/api/reports${projectId ? `?projectId=${projectId}` : ""}`),
    create: (payload: { projectId: UUID; title: string; data: string; format: ReportFormat }) =>
      request<Report>("/api/reports", { method: "POST", body: JSON.stringify(payload) })
  },
  youtube: {
    importComments: (payload: { projectId: UUID; video: string; maxResults: number }) =>
      request<{ sourceId: UUID; videoId: string; importedCount: number }>("/api/youtube/comments/import", {
        method: "POST",
        body: JSON.stringify(payload)
      })
  }
};
