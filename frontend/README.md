# Frontend (React + TypeScript)

Рабочий SPA для backend API `sentimentum`.

## Запуск

```bash
npm install
npm run dev
```

По умолчанию Vite стартует на `http://localhost:5173` и проксирует `/api` в backend на `http://localhost:8080`.

Для другого адреса backend можно задать:

```bash
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

В Docker Compose прокси `/api` направляется на backend-контейнер через `VITE_BACKEND_PROXY_TARGET=http://backend:8080`.
