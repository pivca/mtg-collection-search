# Render Deployment Plan

## Summary

- Deploy as one Render Web Service that serves both the React frontend and Spring Boot backend from the same public `onrender.com` URL.
- Use Render PostgreSQL for persistence and Render environment variables for Discord/database secrets.
- This avoids cross-origin OAuth/session-cookie problems while there is no custom domain.
- Target v1 is lowest cost first, accepting cold starts and possible slower scraping.

## Key Changes

- Package the frontend into the backend deploy:
  - Add a repo-root multi-stage `Dockerfile`.
  - Build `mtgcs-fe-app` with Node, copy `dist/` into Spring Boot static resources, then build/run `mtgcs-app`.
  - Use a Playwright Java Docker base image pinned to the backend Playwright version, because the backend scraper launches Chromium.
- Make Spring Boot internet-ready:
  - Change `server.port` to use `${PORT:8090}` so Render can inject its runtime port.
  - Change datasource config to use `${DB_URL:jdbc:postgresql://localhost:5432/mtgcs}` plus `DB_USERNAME`/`DB_PASSWORD`.
  - Add `server.forward-headers-strategy: framework` so OAuth redirect URLs use public HTTPS behind Render's proxy.
  - Add a lightweight public health endpoint, e.g. `GET /health`, for Render health checks.
  - Add an SPA fallback so direct visits to frontend routes like `/friends`, `/search`, and `/error` serve `index.html`, while `/api/**`, `/oauth2/**`, `/login/**`, and `/logout` keep backend behavior.
- Keep frontend API paths unchanged:
  - Keep `BASE = '/api'`.
  - Keep Discord login links as `/oauth2/authorization/discord`.
  - Because frontend and backend are same-origin in production, no frontend env var is needed for the API base URL.
- Secrets:
  - Rotate the Discord client secret and bot token before deployment.
  - Keep local `.env` out of git; it is already covered by `mtgcs-app/.gitignore`.
  - Store production values only in Render environment variables.

## Render Setup

- Create a Render PostgreSQL database in the closest/cheapest acceptable region.
- Create one Render Web Service:
  - Runtime: Docker.
  - Root directory: repository root.
  - Dockerfile path: `./Dockerfile`.
  - Health check path: `/health`.
  - Instance type: lowest viable Render web service tier for v1.
- Set backend env vars:
  - `PORT=10000` if Render does not provide it automatically.
  - `DB_URL=jdbc:postgresql://<render-internal-host>:5432/<database>`
  - `DB_USERNAME=<render-db-user>`
  - `DB_PASSWORD=<render-db-password>`
  - `DISCORD_CLIENT_ID=<rotated-client-id>`
  - `DISCORD_CLIENT_SECRET=<rotated-client-secret>`
  - `DISCORD_BOT_TOKEN=<rotated-bot-token>`
  - `FRONTEND_URL=https://<render-web-service-name>.onrender.com`
- In the Discord Developer Portal, add this OAuth redirect URI:
  - `https://<render-web-service-name>.onrender.com/login/oauth2/code/discord`

## Test Plan

- Local checks:
  - `npm run test -- --run` in `mtgcs-fe-app`.
  - `npm run build` in `mtgcs-fe-app`.
  - `./mvnw -DskipTests package` in `mtgcs-app`.
  - Docker build from repo root.
  - Run the Docker image locally with local Postgres env vars and confirm `/health`, `/`, `/friends`, `/api/users/me`, and Discord login redirect behavior.
- Production smoke test:
  - Open the Render URL.
  - Login with Discord.
  - Confirm redirect returns to the app, session cookie works, `/api/users/me` returns the user, and refreshing `/friends` does not 404.
  - Add/search a collection to confirm DB writes and Playwright scraping work in the deployed container.

## Assumptions

- V1 should prioritize lowest cost first, accepting cold starts and possible slower scraping.
- No custom domain for now; the app will use the Render-provided URL.
- One deployed web service is preferred over separate frontend/backend services until a custom domain is added, because it keeps OAuth and cookies simple.
- Spring's default single-instance session behavior is acceptable for v1; persistent/distributed sessions can be added later if restarts or scaling become a problem.
