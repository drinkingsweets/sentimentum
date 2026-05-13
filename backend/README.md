# Backend (Java Spring Boot)

REST API для пилота Sentimentum: проекты, источники данных, сообщения,
результаты анализа тональности, отчеты и аудит.

## Запуск локально

```bash
mvn spring-boot:run
```

По умолчанию используется in-memory H2:

- API: `http://localhost:8080/api`
- H2 console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:sentimentum`

## Проверка

```bash
mvn test
```

## Основные endpoints

- `GET/POST /api/users`
- `GET /api/users/me`
- `GET/POST /api/projects`
- `GET/POST /api/data-sources`
- `GET/POST /api/messages`
- `GET/POST /api/analysis-results`
- `GET /api/analytics/sentiment-stats?projectId={uuid}`
- `GET/POST /api/reports`
- `GET/POST /api/audit-logs`
- `POST /api/youtube/comments/import`

## Авторизация

Регистрация пользователя открыта:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Analyst","email":"analyst@example.com","password":"secret"}'
```

Пароль сохраняется в БД как BCrypt-хеш. Остальные endpoints требуют HTTP Basic:

```bash
curl -u analyst@example.com:secret http://localhost:8080/api/projects
```

Проекты, источники, сообщения, результаты анализа, отчеты и аудит фильтруются по текущему пользователю.
Создать проект для другого пользователя через request body нельзя.

## Импорт комментариев YouTube

Нужен ключ YouTube Data API:

```bash
export YOUTUBE_API_KEY=...
```

Пример импорта:

```bash
curl -u analyst@example.com:secret \
  -X POST http://localhost:8080/api/youtube/comments/import \
  -H "Content-Type: application/json" \
  -d '{"projectId":"PROJECT_UUID","video":"https://www.youtube.com/watch?v=VIDEO_ID","maxResults":50}'
```

## Пример минимального сценария

1. Создать пользователя в `/api/users`.
2. Создать проект с `ownerId` в `/api/projects`.
3. Добавить источник данных в `/api/data-sources`.
4. Добавить сообщения в `/api/messages`.
5. Записать результаты классификации в `/api/analysis-results`.
6. Посмотреть агрегированную тональность через `/api/analytics/sentiment-stats`.
