# Cheap Reliable Internet Hosting Plan

## Summary

Recommended path: deploy the app as **one Dockerized full-stack service on Render**, with the React build served by the Spring Boot backend and a managed PostgreSQL database.

This fits the current repo best because the app needs Java 21, PostgreSQL, Discord OAuth, cookie sessions, and Playwright/Chromium scraping. A single same-origin deployment avoids CORS, OAuth redirect, and cookie problems.

Hosting recommendation ranking:

1. **Render, recommended**: simplest managed deployment, static + backend can run as one Docker service, managed Postgres available. Render web services start at free/$7/$25 tiers, static sites are free, and Postgres is available from free with paid backup/PITR features. Source: https://render.com/pricing
2. **DigitalOcean App Platform**: reliable and predictable; static frontend can be free, backend containers start around $5-$25/month, dev Postgres is $7/month but has limited reliability/backups. Source: https://www.digitalocean.com/pricing/app-platform
3. **Railway**: very easy and often cheap, but usage-based billing is less predictable; Hobby is $5 minimum usage and may have deployment/resource prioritization limits. Sources: https://docs.railway.com/reference/pricing and https://docs.railway.com/deployments/reference
4. **Fly.io**: powerful for containers, but Postgres is self-managed/unsupported on Fly Machines; better for later, not v1. Source: https://fly.io/docs/about/pricing/
5. **Hetzner VPS**: cheapest raw compute, but not the recommended managed-cheap option because you own OS patching, Docker, Postgres backups, monitoring, TLS, and recovery. Source: https://www.hetzner.com/cloud/

## Key Changes

- Add a repo-root Docker deployment:
  - Multi-stage build: build `mtgcs-fe-app`, copy `dist/` into the Spring Boot app, then package/run `mtgcs-app`.
  - Use a Playwright-compatible Java base image or install Chromium/system dependencies explicitly, because `DeckboxScraper` launches headless Chromium.
  - Run the Spring Boot jar as the only public service.

- Make backend production-ready:
  - Change `server.port` to `${PORT:8090}`.
  - Change datasource config to accept a full production JDBC URL, e.g. `${DB_URL:jdbc:postgresql://localhost:5432/mtgcs}`.
  - Add `server.forward-headers-strategy: framework` so Discord OAuth redirects use public HTTPS behind the host proxy.
  - Add a real public `GET /health` endpoint for platform health checks.
  - Add SPA fallback routing for frontend routes while preserving `/api/**`, `/oauth2/**`, `/login/**`, and `/logout`.

- Keep production same-origin:
  - Keep frontend API base as `/api`.
  - Keep login redirect path as `/oauth2/authorization/discord`.
  - Set `FRONTEND_URL` to the deployed public origin.
  - In Discord Developer Portal, configure redirect URI:
    `https://<app-host>/login/oauth2/code/discord`

- Use managed secrets:
  - Store `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DISCORD_CLIENT_ID`, `DISCORD_CLIENT_SECRET`, `DISCORD_BOT_TOKEN`, and `FRONTEND_URL` in the hosting provider.
  - Rotate Discord client secret and bot token before production launch.

## Render Deployment

- Create one Render PostgreSQL database in the same region as the app.
- Create one Render Web Service:
  - Runtime: Docker.
  - Root directory: repo root.
  - Dockerfile path: `./Dockerfile`.
  - Health check path: `/health`.
  - Start with a **2 GB instance** if Chromium scraping fails or is unstable on 512 MB; Java + Playwright is likely too tight for 512 MB.
- Use the Render-provided URL for v1 unless you already own a domain.
- If using a custom domain later, update:
  - Render custom domain.
  - `FRONTEND_URL`.
  - Discord OAuth redirect URI.

## Test Plan

- Local verification:
  - `npm run test -- --run` in `mtgcs-fe-app`.
  - `npm run build` in `mtgcs-fe-app`.
  - `./mvnw test` in `mtgcs-app`.
  - Docker build from repo root.
  - Run the image locally against local Postgres and verify `/health`, `/`, `/friends`, `/api/users/me`, and OAuth redirect behavior.

- Production smoke test:
  - Open the public URL.
  - Login with Discord.
  - Confirm redirect returns to the app.
  - Confirm session cookie works across page refresh.
  - Confirm `/api/users/me` returns the logged-in user.
  - Add a friend/collection and verify PostgreSQL writes.
  - Run a collection search and confirm Playwright scraping works in the hosted container.

## Assumptions

- Priority is **cheap and reliable**, not zero-cost.
- The first production version should use a managed platform, not a self-managed VPS.
- One same-origin full-stack deployment is preferred over separate frontend/backend hosts.
- A Render-provided URL is acceptable for v1; custom domain can be added later.
- Single-instance Spring session behavior is acceptable for v1; distributed sessions are only needed if the app scales horizontally.
