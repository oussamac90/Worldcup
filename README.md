# GoalKeeper Dash

A casual arcade goalkeeping game whose retention engine is **national supremacy**:
every player picks a nation and their scores feed both a personal-within-nation
rank and their nation's rank in the world. Built per the v1 production spec.

- **Backend** — Spring Boot (Java 21), PostgreSQL, Redis. Entire stack comes up with one `docker compose up`.
- **Mobile** — Expo (React Native + TypeScript), launched separately (`npx expo start`).

```
.
├── backend/            Spring Boot API + Thymeleaf back-office (one deployable jar)
├── mobile/             Expo app (game engine, screens, typed API client)
├── docker-compose.yml  db + redis + api (+ optional adminer)
├── .env.example        copy to .env and fill secrets
└── api.http            REST Client collection: auth → session → submit → leaderboards
```

## Architecture

The backend is a modular monolith. Modules are separate packages that talk only
through service interfaces (no reaching across internals):

| Module        | Responsibility                                               |
|---------------|-------------------------------------------------------------|
| `user`        | accounts, IdP (OIDC) auth, nation binding, profile, nations |
| `leaderboard` | seasons, score aggregation, Redis ranking, dual-board reads |
| `game`        | game sessions, score submission, sanity validation          |
| `backoffice`  | Thymeleaf admin (moderation, season control, analytics)     |
| `common`      | config, security, error envelope, Redis, rate limiting      |

**Dependency rule:** `game → leaderboard, user`; `leaderboard → user`;
`user → common only`. Cross-module season context is read via a `SeasonService`
interface declared in `common`, so `user` never depends on `leaderboard`.

**Data flow of a run:** authenticate → `POST /sessions` (server returns
`sessionId` + `nonce` + `seed`) → play locally → `POST /sessions/{id}/submit` →
sanity validation → persist immutable `ScoreSubmission` → upsert Postgres
aggregates → (after commit) update Redis ZSETs → client reads `/leaderboards/me`.

- **Postgres** is the source of truth (aggregates + the immutable submission audit log).
- **Redis** is a rebuildable index (sorted sets). It can be wiped at any time and
  rebuilt from Postgres (`/admin/leaderboards` → Rebuild, or automatically on read).
- **National total** = sum of each contributing user's season `bestScore`
  (rewards breadth, resists grind-spam — documented in `NationSeasonStat`).

## Runbook

### 1. Backend (one command)

```bash
cp .env.example .env      # then fill IdP client IDs + real secrets
docker compose up         # builds the api image, starts db + redis + api
```

- Schema self-builds via Hibernate (`ddl-auto=update`); the startup **seeder**
  loads 48 nations (incl. `MAR`), an active "Season 1", the bootstrap admin, and
  (by default) simulated leaderboard data so the boards aren't empty.
- API: `http://localhost:8080/api/v1`
- Health: `http://localhost:8080/actuator/health` (readiness depends on db + redis)
- Back-office: `http://localhost:8080/admin` (log in with `ADMIN_BOOTSTRAP_USER` /
  `ADMIN_BOOTSTRAP_PASSWORD` from `.env`)
- Optional DB UI: `docker compose --profile tools up` → Adminer on `:8090`

Try the flow with `api.http` (VS Code REST Client) or curl. A dev login path
(`POST /api/v1/auth/dev`, gated by `APP_OIDC_DEV_LOGIN_ENABLED`) lets you exercise
everything without real IdP credentials.

### 2. Mobile

```bash
cd mobile
cp .env.example .env       # EXPO_PUBLIC_API_BASE_URL defaults to http://localhost:8080
npm install
npx expo start
```

Sign in (Google / Facebook / Apple, or the dev-login button), pick a nation,
play, and watch both ranks update. See `mobile/README.md` for IdP client-ID setup.

### 3. Tests

```bash
cd backend
./gradlew test            # unit tests + Testcontainers integration tests
```

- **Unit:** sanity validator, JWT issue/parse (run anywhere).
- **Integration:** `SubmitFlowIT` exercises auth → nation → session → submit →
  dual leaderboard against **real** Postgres + Redis via Testcontainers
  (requires Docker; pulls `postgres:16` and `redis:7`).

## Configuration

All config is environment-driven (`.env` → compose → Spring). Key vars are in
`.env.example`. Notable ones:

| Var | Purpose |
|-----|---------|
| `APP_JWT_SECRET` | HMAC signing key for access tokens (**≥ 32 bytes**) |
| `GOOGLE_CLIENT_ID` / `FACEBOOK_APP_ID` / `APPLE_CLIENT_ID` | server-side OIDC token verification |
| `APP_OIDC_DEV_LOGIN_ENABLED` | enables the unverified dev login path (**false in prod**) |
| `APP_SEED_ENABLED` / `APP_SEED_SIMULATED_NATIONS` | startup seeding toggles |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` (dev) / `validate` (prod) |

## Prod notes

- **`SPRING_JPA_HIBERNATE_DDL_AUTO=validate`** — never auto-`update` in prod; make
  schema changes a deliberate, reviewed action (avoids silent schema drift).
- **`APP_SEED_ENABLED=false`** — don't run the seeder in prod.
- **`APP_OIDC_DEV_LOGIN_ENABLED=false`** — disable the dev login path.
- Supply real secrets (`APP_JWT_SECRET`, IdP client IDs) via your secret manager.
- Compose serves plain HTTP locally; **terminate TLS at the edge** (LB/ingress) in prod.
- Redis is a cache/index; it's safe to lose — it rebuilds from Postgres.

## Status vs. spec

Backend milestones M0–M7 and M9 are implemented and verified end-to-end against
real Postgres + Redis (auth, nation lock, sessions, sanity flagging, dual
leaderboards, exact Redis rebuild, admin back-office, JSON logging with traceId).
The Expo client (M8) lives in `mobile/` and typechecks. The only piece not
runnable in the build sandbox was a full `docker compose up` and the Testcontainers
suite, because Docker Hub image pulls were rate-limited there; the compose file is
validated (`docker compose config`) and the app was verified against locally-run
Postgres 16 + Redis 7.
