# GoalKeeper Dash — Mobile Client

Expo (React Native + TypeScript) client for GoalKeeper Dash. Players are a
goalkeeper blocking incoming shots; every score contributes to both a
personal rank within the player's chosen nation and that nation's rank in
the world.

## Getting started

```bash
cp .env.example .env
npm install
npx expo start
```

Then press `i` / `a` / `w` in the Expo CLI to open iOS Simulator, an Android
emulator, or a web build, or scan the QR code with Expo Go / a dev client.

Run a standalone typecheck at any time with:

```bash
npx tsc --noEmit
```

## Configuration

All runtime configuration is read from environment variables prefixed with
`EXPO_PUBLIC_` (see `app.config.ts`, which forwards them into `expo-constants`
`extra`). Copy `.env.example` to `.env` and fill in the values you need:

| Variable | Purpose |
| --- | --- |
| `EXPO_PUBLIC_API_BASE_URL` | Base URL of the GoalKeeper Dash backend. All API calls are made to `${EXPO_PUBLIC_API_BASE_URL}/api/v1/...`. Defaults to `http://localhost:8080`. |
| `EXPO_PUBLIC_GOOGLE_CLIENT_ID_IOS` | OAuth client ID (iOS) for Google Sign-In, used by `expo-auth-session`. |
| `EXPO_PUBLIC_GOOGLE_CLIENT_ID_ANDROID` | OAuth client ID (Android) for Google Sign-In. |
| `EXPO_PUBLIC_GOOGLE_CLIENT_ID_WEB` | OAuth client ID (Web) for Google Sign-In; also used as the fallback "Expo Go" client ID during development. |
| `EXPO_PUBLIC_FACEBOOK_APP_ID` | Facebook App ID used to build the Facebook Limited Login (OIDC id_token) request. |
| `EXPO_PUBLIC_EAS_PROJECT_ID` | Optional. Only required for EAS builds/updates. |

Apple Sign-In (`expo-apple-authentication`) needs no client ID — it's wired
up via the `usesAppleSignIn` flag in `app.config.ts` and only appears as an
option on devices that support it (checked at runtime).

### If you don't have real IdP credentials yet

The auth screen always shows a **"Dev login"** button whenever Google or
Facebook isn't fully configured. It calls `POST /api/v1/auth/dev` with a
placeholder id token, which the backend's dev login path is expected to
accept and exchange for a real app session (access + refresh JWTs) — this
lets you exercise the whole app without setting up OAuth consoles first.

## Project layout

```
app/                      expo-router routes (screens are thin; logic lives in src/)
  _layout.tsx              root layout: providers + auth-aware redirect logic
  index.tsx                transient splash while auth state resolves
  (auth)/index.tsx         sign-in screen (Google / Facebook / Apple / dev)
  nation-picker.tsx        nation grid, shown when the user has no nation yet
  (app)/index.tsx          home screen: dual leaderboard + mode picker + Play
  (app)/play.tsx           opens a session, runs the engine, submits the score

src/
  engine/
    core/                  fixed-timestep loop, seedable RNG, state machine,
                            the deterministic Simulation itself — zero React
                            or network imports anywhere in this subtree
    entities/               keeper, shots, powerups, particles (pure data + functions)
    modes/                  Tournament / Survival / Sudden Death / Shootout configs
    render/                 React + Reanimated rendering layer; reads simulation
                            state, never owns game logic
    audio/                  SFX playback wrapper with a mute toggle
  net/                      typed API client (auth, profile, sessions, leaderboards),
                            token storage (expo-secure-store), automatic 401 refresh
  state/                    AuthContext + small data hooks used by screens
  components/               shared presentational components (Button, Card, RankPanel)
  theme/                    color tokens
```

### Why the engine is split from rendering

`src/engine/core/simulation.ts` advances game state in fixed 1000/60 ms ticks
given only a numeric seed and a stream of input actions (`dive`, `moveTo`).
Nothing in `engine/core`, `engine/entities`, or `engine/modes` imports React
or performs network I/O — the simulation is a pure function of
`(seed, actions over time)`. The render layer (`engine/render`) is the only
part that touches React/Reanimated; it drives the simulation via
`useGameLoop` and renders whatever `simulation.state` currently holds. The
session's seed is requested from the server at `POST /api/v1/sessions` and
threaded into `eventSummary` indirectly via the session/submit flow — this
keeps a future "replay this exact run" feature a matter of re-feeding the
same seed and input log into `Simulation`, with no rendering code involved.

## Backend contract

The client expects a backend mounted at `${EXPO_PUBLIC_API_BASE_URL}/api/v1`
implementing: `auth/{google,facebook,apple,dev}`, `auth/refresh`,
`auth/logout`, `me` (+ `PATCH`/`PUT .../nation`), `nations`, `sessions` (+
`:id/submit`), and `leaderboards/{me,nations,nations/:code/users,users}`. See
`src/net/types.ts` for the exact request/response shapes the client encodes
and decodes.

## Notes

- SFX are wired through `src/engine/audio/SoundManager`, but no audio assets
  are bundled yet. Drop files under `assets/sfx/` and register them with
  `SoundManager.shared.registerSource(key, require('../assets/sfx/save.mp3'))`
  during app bootstrap; until then, `play()` calls are silent no-ops.
- `npx tsc --noEmit` is run in strict mode with no `any` anywhere in `src/`.
