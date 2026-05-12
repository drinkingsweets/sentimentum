# Docker configs

Запуск backend API и необходимых сервисов:

```bash
docker compose up --build
```

Сервисы:

- `backend` - Spring Boot API на `http://localhost:8080`
- `postgres` - PostgreSQL на `localhost:5432`

Переменные окружения можно переопределить перед запуском:

```bash
POSTGRES_DB=sentimentum POSTGRES_USER=sentimentum POSTGRES_PASSWORD=sentimentum BACKEND_PORT=8080 docker compose up --build
```

Данные PostgreSQL хранятся в volume `postgres_data`.
