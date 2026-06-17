import type { Rng } from "../core/rng";
import type { LaneX, ShotKind, ShotOutcome } from "../core/types";

export interface ShotState {
  id: number;
  kind: ShotKind;
  targetX: LaneX;
  /** Current visual/sim progress from 0 (struck) to 1 (reaches goal line). */
  progress: number;
  travelTimeMs: number;
  /** For "fake" shots: the lane the shot feints toward before committing to targetX. */
  fakeX: LaneX | null;
  fakeSwitchAt: number; // progress threshold (0..1) at which fake resolves to real target
  /** For "deflection" shots: a secondary bounce lane after first contact. */
  deflectAt: number | null;
  deflectX: LaneX | null;
  resolved: boolean;
  outcome: ShotOutcome | null;
}

export interface ShotProfile {
  kind: ShotKind;
  minTravelMs: number;
  maxTravelMs: number;
  weight: number; // relative spawn weight
}

export const DEFAULT_SHOT_PROFILES: readonly ShotProfile[] = [
  { kind: "ground", minTravelMs: 900, maxTravelMs: 1300, weight: 5 },
  { kind: "header", minTravelMs: 700, maxTravelMs: 1000, weight: 3 },
  { kind: "fake", minTravelMs: 1100, maxTravelMs: 1500, weight: 2 },
  { kind: "deflection", minTravelMs: 1000, maxTravelMs: 1400, weight: 2 },
  { kind: "rocket", minTravelMs: 450, maxTravelMs: 650, weight: 1 },
];

let nextShotId = 1;

export function resetShotIdCounter(): void {
  nextShotId = 1;
}

export function pickShotProfile(
  rng: Rng,
  profiles: readonly ShotProfile[] = DEFAULT_SHOT_PROFILES,
): ShotProfile {
  const totalWeight = profiles.reduce((sum, p) => sum + p.weight, 0);
  let roll = rng.nextRange(0, totalWeight);
  for (const profile of profiles) {
    roll -= profile.weight;
    if (roll <= 0) return profile;
  }
  return profiles[profiles.length - 1];
}

export function spawnShot(
  rng: Rng,
  profile: ShotProfile,
  speedMultiplier = 1,
): ShotState {
  const targetX = rng.nextRange(0.06, 0.94);
  const travelTimeMs =
    rng.nextRange(profile.minTravelMs, profile.maxTravelMs) / speedMultiplier;

  let fakeX: LaneX | null = null;
  let fakeSwitchAt = 0;
  if (profile.kind === "fake") {
    fakeX = rng.nextRange(0.06, 0.94);
    fakeSwitchAt = rng.nextRange(0.45, 0.65);
  }

  let deflectAt: number | null = null;
  let deflectX: LaneX | null = null;
  if (profile.kind === "deflection") {
    deflectAt = rng.nextRange(0.55, 0.75);
    deflectX = rng.nextRange(0.06, 0.94);
  }

  return {
    id: nextShotId++,
    kind: profile.kind,
    targetX,
    progress: 0,
    travelTimeMs,
    fakeX,
    fakeSwitchAt,
    deflectAt,
    deflectX,
    resolved: false,
    outcome: null,
  };
}

/** Advances a shot's flight progress; returns a new ShotState. */
export function stepShot(shot: ShotState, dtMs: number): ShotState {
  if (shot.resolved) return shot;
  const progress = Math.min(1, shot.progress + dtMs / shot.travelTimeMs);
  return { ...shot, progress };
}

/** The lane the shot currently occupies, accounting for fakes/deflections. */
export function currentShotLane(shot: ShotState): LaneX {
  if (shot.kind === "fake" && shot.fakeX !== null) {
    return shot.progress < shot.fakeSwitchAt ? shot.fakeX : shot.targetX;
  }
  if (shot.kind === "deflection" && shot.deflectAt !== null && shot.deflectX !== null) {
    return shot.progress < shot.deflectAt ? shot.targetX : shot.deflectX;
  }
  return shot.targetX;
}

export function isShotArrived(shot: ShotState): boolean {
  return shot.progress >= 1;
}
