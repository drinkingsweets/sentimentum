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
- `GET/POST /api/projects`
- `GET/POST /api/data-sources`
- `GET/POST /api/messages`
- `GET/POST /api/analysis-results`
- `GET /api/analytics/sentiment-stats?projectId={uuid}`
- `GET/POST /api/reports`
- `GET/POST /api/audit-logs`

## Пример минимального сценария

1. Создать пользователя в `/api/users`.
2. Создать проект с `ownerId` в `/api/projects`.
3. Добавить источник данных в `/api/data-sources`.
4. Добавить сообщения в `/api/messages`.
5. Записать результаты классификации в `/api/analysis-results`.
6. Посмотреть агрегированную тональность через `/api/analytics/sentiment-stats`.
