# Deploying HealthHub on Infomaniak Cloud

HealthHub ships as **one Docker image** (Spring Boot backend + built Vue SPA served same-origin)
plus a **PostgreSQL** database. Infomaniak offers two ways to run that; both are covered below.
Secrets are **only ever environment variables** — never baked into the image, never committed.

## What gets deployed

| Piece | Where |
| --- | --- |
| `infra/docker/Dockerfile` | multi-stage build: Maven (JDK 25) → backend jar, Node 22 → `frontend/dist`, Temurin 25 JRE runtime. CI builds and pushes it to `ghcr.io/cyrilsx/inside-my-skin` on every `main` push |
| `infra/docker-compose.prod.yml` | `app` + `postgres:17` with healthchecks, `restart: unless-stopped`, named volume for pgdata |
| `backend/app/src/main/resources/application-prod.yml` | prod profile: `ddl-auto: validate`, Flyway on, actuator limited to `/actuator/health` without details, `forward-headers-strategy: framework` (for Jelastic TLS termination), graceful shutdown |
| Security headers | CSP (`default-src 'self'` …), `Referrer-Policy: no-referrer`, HSTS over HTTPS (Spring Security, behind the proxy), `X-Frame-Options`/`nosniff` defaults |
| SPA fallback | `SpaConfig` serves `index.html` for client routes (`/trends`, …); `/api/**`, `/actuator/**`, `/swagger*`, `/v3/api-docs`, `/openapi.yaml`, `/webhooks/**`, `/connect/**` are never rewritten |

## Option A — Infomaniak Public Cloud (Jelastic PaaS), one-click

1. In the Jelastic dashboard (Infomaniak Public Cloud), import `infra/infomaniak/manifest.jps`
   (Marketplace → Install → paste manifest). It creates a **docker node** (image from GHCR)
   and a **managed PostgreSQL 17 node**.
2. Fill the required env vars when prompted (or afterwards under *Settings → Environment
   variables*, stored encrypted by Jelastic): `JWT_SIGNING_KEY`, `TOKEN_ENCRYPTION_KEY`,
   `WEBHOOK_SECRET`, `POSTGRES_PASSWORD`, `WITHINGS_REDIRECT_URI`.
   Generate secrets with: `head -c 32 /dev/urandom | base64`.
3. Add a custom domain and enable **Let's Encrypt** (Settings → Custom domains) — Jelastic
   terminates TLS and forwards `X-Forwarded-*`, which the prod profile trusts.
4. Open `https://<your-env>.infomaniak.cloud/` — dashboard; `…/swagger-ui` — API docs
   (JWT required for `/api/**`).

If you don't use the manifest, create the environment manually with the same two nodes and
the env vars above.

## Option B — Infomaniak Cloud Server (VM) with Docker

```bash
ssh root@<your-cloud-server>
apt-get update && apt-get install -y docker.io docker-compose-plugin git
 git clone https://github.com/cyrilsx/inside-my-skin.git && cd inside-my-skin
cp .env.prod.example .env.prod && chmod 600 .env.prod
$EDITOR .env.prod        # fill all secrets + WITHINGS_REDIRECT_URI=https://<domain>/connect/withings/callback
docker compose -f infra/docker-compose.prod.yml up -d --build
# TLS: put Caddy in front, e.g. Caddyfile:
#   your-domain.example {
#     reverse_proxy localhost:8080
#   }
# (Caddy auto-issues Let's Encrypt certificates), or use nginx + certbot.
```

`docker compose -f infra/docker-compose.prod.yml config` validates the file; the CI
`compose` job does the same on every PR.

## Secrets checklist (both options)

- [ ] `POSTGRES_PASSWORD`, `JWT_SIGNING_KEY`, `TOKEN_ENCRYPTION_KEY`, `WEBHOOK_SECRET` — random, 256-bit base64, set as env vars only
- [ ] Provider credentials: prefer the dashboard **Sources → Provider credentials** (stored AES-GCM encrypted, master key = `TOKEN_ENCRYPTION_KEY`); env fallback (`GARMIN_*`, `WITHINGS_*`) supported
- [ ] `WITHINGS_REDIRECT_URI` must be the public `https://…/connect/withings/callback` URL, and registered in the Withings developer console
- [ ] No secret in git: `infra/scripts/scan-secrets.sh` runs in CI on every push; `.env.prod` is gitignored

## Operations

- Health: `GET /actuator/health` (public, used by the container healthcheck)
- Logs: redacted by construction (M11) — `Redactor` masks tokens/secrets; requestId per request. On Jelastic: dashboard → Logs. On a VM: `docker compose -f infra/docker-compose.prod.yml logs -f app`
- Backups: Jelastic managed PostgreSQL has built-in backups; on a VM use `pg_dump` via `docker compose exec postgres pg_dump -U healthhub healthhub > backup.sql` (cron it)
- Upgrades: push to `main` → CI builds `ghcr.io/…:<sha>` + `:latest` → Jelastic redeploy / `docker compose pull && up -d` on the VM. Flyway migrates on boot; `ddl-auto: validate` fails fast on drift
- Rollback: redeploy the previous image tag
