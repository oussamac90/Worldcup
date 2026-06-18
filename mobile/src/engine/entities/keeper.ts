import type { LaneX } from "../core/types";

/** Keeper occupies a continuous position across the goal mouth, plus a dive state. */
export type DiveDirection = "none" | "left" | "right" | "stay";

export interface KeeperState {
  x: LaneX; // 0..1 across the goal mouth
  diving: boolean;
  diveDirection: DiveDirection;
  diveProgress: number; // 0..1, 1 = fully extended
  diveReachExtra: number; // bonus reach from powerups (wideArms)
  recoveryMs: number; // time left before keeper can dive again
}

export const KEEPER_DIVE_DURATION_MS = 260;
export const KEEPER_RECOVERY_MS = 180;
export const KEEPER_BASE_REACH = 0.16; // fraction of goal width reachable without diving
export const KEEPER_DIVE_REACH = 0.42; // fraction of goal width reachable at full dive

export function createKeeper(): KeeperState {
  return {
    x: 0.5,
    diving: false,
    diveDirection: "none",
    diveProgress: 0,
    diveReachExtra: 0,
    recoveryMs: 0,
  };
}

export function startDive(keeper: KeeperState, direction: DiveDirection): KeeperState {
  if (keeper.diving || keeper.recoveryMs > 0) return keeper;
  return {
    ...keeper,
    diving: true,
    diveDirection: direction,
    diveProgress: 0,
  };
}

export function stepKeeper(keeper: KeeperState, dtMs: number): KeeperState {
  let next = keeper;

  if (next.recoveryMs > 0) {
    next = { ...next, recoveryMs: Math.max(0, next.recoveryMs - dtMs) };
  }

  if (next.diving) {
    const progress = Math.min(1, next.diveProgress + dtMs / KEEPER_DIVE_DURATION_MS);
    if (progress >= 1) {
      next = {
        ...next,
        diving: false,
        diveProgress: 0,
        diveDirection: "none",
        recoveryMs: KEEPER_RECOVERY_MS,
      };
    } else {
      next = { ...next, diveProgress: progress };
    }
  }

  return next;
}

/** Current reach of the keeper around `keeper.x`, in normalized goal-width units. */
export function currentReach(keeper: KeeperState): number {
  const base = KEEPER_BASE_REACH + keeper.diveReachExtra;
  if (!keeper.diving) return base;
  const diveBonus = (KEEPER_DIVE_REACH - KEEPER_BASE_REACH) * keeper.diveProgress;
  return base + diveBonus;
}

/** Whether the keeper can currently make a save at lane position `targetX`. */
export function canReach(keeper: KeeperState, targetX: LaneX): boolean {
  return Math.abs(keeper.x - targetX) <= currentReach(keeper);
}
