import type { Rng } from "../core/rng";
import type { LaneX, PowerupKind } from "../core/types";

export type { PowerupKind } from "../core/types";

export interface PowerupState {
  id: number;
  kind: PowerupKind;
  x: LaneX;
  spawnedAtMs: number;
  expiresAtMs: number;
  collected: boolean;
}

export interface ActivePowerup {
  kind: PowerupKind;
  remainingMs: number;
}

export const POWERUP_KINDS: readonly PowerupKind[] = [
  "slowMotion",
  "wideArms",
  "doublePoints",
  "magnet",
];

export const POWERUP_LIFETIME_MS = 4000;
export const POWERUP_EFFECT_DURATION_MS: Record<PowerupKind, number> = {
  slowMotion: 5000,
  wideArms: 6000,
  doublePoints: 8000,
  magnet: 5000,
};

let nextPowerupId = 1;

export function resetPowerupIdCounter(): void {
  nextPowerupId = 1;
}

export function spawnPowerup(rng: Rng, nowMs: number): PowerupState {
  return {
    id: nextPowerupId++,
    kind: rng.pick(POWERUP_KINDS),
    x: rng.nextRange(0.1, 0.9),
    spawnedAtMs: nowMs,
    expiresAtMs: nowMs + POWERUP_LIFETIME_MS,
    collected: false,
  };
}

export function isPowerupExpired(powerup: PowerupState, nowMs: number): boolean {
  return !powerup.collected && nowMs >= powerup.expiresAtMs;
}

export function applyPowerupTick(
  active: readonly ActivePowerup[],
  dtMs: number,
): ActivePowerup[] {
  return active
    .map((p) => ({ ...p, remainingMs: p.remainingMs - dtMs }))
    .filter((p) => p.remainingMs > 0);
}

export function activatePowerup(
  active: readonly ActivePowerup[],
  kind: PowerupKind,
): ActivePowerup[] {
  const duration = POWERUP_EFFECT_DURATION_MS[kind];
  const withoutKind = active.filter((p) => p.kind !== kind);
  return [...withoutKind, { kind, remainingMs: duration }];
}

export function hasActivePowerup(
  active: readonly ActivePowerup[],
  kind: PowerupKind,
): boolean {
  return active.some((p) => p.kind === kind);
}
