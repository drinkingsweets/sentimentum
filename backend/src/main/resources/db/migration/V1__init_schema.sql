CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE permission_bank (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE user_permissions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    permission_id UUID NOT NULL REFERENCES permission_bank(id),
    CONSTRAINT uk_user_permission UNIQUE (user_id, permission_id)
);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE users_projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    role_in_company VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_user_project UNIQUE (user_id, project_id)
);

CREATE TABLE data_sources (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(2048) NOT NULL,
    type VARCHAR(64) NOT NULL,
    project_id UUID NOT NULL REFERENCES projects(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(255),
    source_id UUID NOT NULL REFERENCES data_sources(id),
    language VARCHAR(32) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE analysis_results (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL REFERENCES messages(id),
    sentiment VARCHAR(64) NOT NULL,
    confidence NUMERIC(5, 4),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reports (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    project_id UUID NOT NULL REFERENCES projects(id),
    title VARCHAR(255) NOT NULL,
    data TEXT NOT NULL,
    format VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    action TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_data_sources_project_id ON data_sources(project_id);
CREATE INDEX idx_messages_source_id ON messages(source_id);
CREATE INDEX idx_analysis_results_message_id ON analysis_results(message_id);
CREATE INDEX idx_reports_project_id ON reports(project_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
