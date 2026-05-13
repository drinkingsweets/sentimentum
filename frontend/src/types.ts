export type UUID = string;

export type Sentiment = "POSITIVE" | "NEUTRAL" | "NEGATIVE" | "AMBIGUOUS";
export type DataSourceType = "YOUTUBE" | "VK" | "TELEGRAM" | "CSV" | "LINK" | "OTHER";
export type ReportFormat = "PDF" | "XLSX" | "PPTX" | "JSON";

export type ApiError = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
};

export type User = {
  id: UUID;
  name: string;
  email: string;
  createdAt: string;
};

export type Project = {
  id: UUID;
  name: string;
  description: string | null;
  ownerId: UUID;
  createdAt: string;
  updatedAt: string;
};

export type DataSource = {
  id: UUID;
  name: string;
  link: string;
  type: DataSourceType;
  projectId: UUID;
  createdAt: string;
};

export type Message = {
  id: UUID;
  content: string;
  author: string | null;
  sourceId: UUID;
  language: string;
  tag: string;
  processedAt: string | null;
  createdAt: string;
};

export type AnalysisResult = {
  id: UUID;
  messageId: UUID;
  sentiment: Sentiment;
  confidence: number | null;
  createdAt: string;
};

export type SentimentStats = {
  sentiment: Sentiment;
  count: number;
};

export type Report = {
  id: UUID;
  userId: UUID;
  projectId: UUID;
  title: string;
  data: string;
  format: ReportFormat;
  createdAt: string;
};
