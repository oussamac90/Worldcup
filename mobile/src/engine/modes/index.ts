import type { GameMode } from "../core/types";

export interface ModeConfig {
  mode: GameMode;
  label: string;
  description: string;
  /** Total time budget in ms, or null if untimed. */
  durationMs: number | null;
  /** Starting lives, or null if not life-based. */
  lives: number | null;
  /** Total number of shots in the session, or null if unlimited (timer/lives govern end). */
  shotLimit: number | null;
  shotIntervalMs: number;
  initialShotDelayMs: number;
  initialPowerupDelayMs: number;
}

const MODE_CONFIGS: Record<GameMode, ModeConfig> = {
  tournament: {
    mode: "tournament",
    label: "Tournament",
    description: "Survive escalating rounds of shots within a fixed time limit.",
    durationMs: 90_000,
    lives: null,
    shotLimit: null,
    shotIntervalMs: 1400,
    initialShotDelayMs: 1200,
    initialPowerupDelayMs: 8000,
  },
  survival: {
    mode: "survival",
    label: "Survival (Time Attack)",
    description: "Rack up saves before the clock runs out. No lives — just score.",
    durationMs: 60_000,
    lives: null,
    shotLimit: null,
    shotIntervalMs: 1100,
    initialShotDelayMs: 900,
    initialPowerupDelayMs: 6000,
  },
  suddenDeath: {
    mode: "suddenDeath",
    label: "Sudden Death",
    description: "One goal conceded and it's over. How long can you last?",
    durationMs: null,
    lives: 1,
    shotLimit: null,
    shotIntervalMs: 1600,
    initialShotDelayMs: 1500,
    initialPowerupDelayMs: 10000,
  },
  shootout: {
    mode: "shootout",
    label: "Shootout",
    description: "Face a fixed set of penalties. Save as many as you can.",
    durationMs: null,
    lives: null,
    shotLimit: 5,
    shotIntervalMs: 1800,
    initialShotDelayMs: 1000,
    initialPowerupDelayMs: 999_999,
  },
};

export function getModeConfig(mode: GameMode): ModeConfig {
  return MODE_CONFIGS[mode];
}

export function listModes(): ModeConfig[] {
  return Object.values(MODE_CONFIGS);
}
