# TaskEngine

Multi-tenant task management with Spring Boot and React.

## Docker

Copy `.env.example` to `.env` and set secrets, then:

```bash
docker compose up --build -d
```

View backend logs:

```bash
docker compose logs -f backend
```

Stop and remove volumes:

```bash
docker compose down -v
```

- **Frontend:** `http://localhost` (nginx → static app, `/api` proxied to backend)
- **Backend API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`

## Local development

- Backend: PostgreSQL on `localhost:5432`, database `taskengine`, then `./mvnw spring-boot:run`
- Frontend: `cd frontend && npm install && npm run dev` (Vite proxies `/api` to the backend)

### Flyway / existing databases

If the database already has tables but no `flyway_schema_history` table (for example schema created outside Flyway), the app enables **`baseline-on-migrate`** so Flyway can create the history table and apply only pending migrations. If you prefer a clean slate, drop and recreate the `taskengine` database, then start the app once.
