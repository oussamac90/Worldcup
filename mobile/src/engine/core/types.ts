/** Shared primitive types used across the simulation engine. */

export interface Vector2 {
  x: number;
  y: number;
}

export type GamePhase = "menu" | "countdown" | "playing" | "paused" | "gameover";

export type GameMode = "tournament" | "survival" | "suddenDeath" | "shootout";

export type ShotKind = "ground" | "header" | "fake" | "deflection" | "rocket";

export type ShotOutcome = "saved" | "perfectSave" | "goal" | "expired";

export type PowerupKind = "slowMotion" | "wideArms" | "doublePoints" | "magnet";

/** Lane positions across the goal mouth, normalized 0 (left post) to 1 (right post). */
export type LaneX = number;

export interface EventSummary {
  shotsFaced: number;
  saves: number;
  goalsConceded: number;
  maxCombo: number;
  perfectSaves: number;
  schemaVersion: 1;
}

export function createEmptyEventSummary(): EventSummary {
  return {
    shotsFaced: 0,
    saves: 0,
    goalsConceded: 0,
    maxCombo: 0,
    perfectSaves: 0,
    schemaVersion: 1,
  };
}
