# Monitoring

Минимальный мониторинг backend поднимается вместе с основным `docker-compose.yml`.

## Запуск

```bash
docker compose up --build
```

## Сервисы

- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- Backend metrics: `http://localhost:8080/actuator/prometheus`

Логин Grafana по умолчанию:

- user: `admin`
- password: `admin`

Можно переопределить через `GRAFANA_ADMIN_USER` и `GRAFANA_ADMIN_PASSWORD`.

## Dashboard

Dashboard `Sentimentum Backend Overview` создается автоматически через Grafana provisioning.

Панели:

- Backend RPS
- HTTP latency p95
- 5xx error rate
- Imported messages per hour
- Created labels per hour
- JVM heap

## Custom metrics

- `sentimentum_imported_messages_total`
- `sentimentum_sentiment_labels_created_total`

Обе метрики имеют тег `source_type`. Метрика разметки также имеет тег `label_mode`.
