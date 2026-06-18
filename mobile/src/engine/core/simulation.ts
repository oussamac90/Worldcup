import { Rng, normalizeSeed } from "./rng";
import { createEmptyEventSummary, type EventSummary, type GameMode } from "./types";
import {
  canReach,
  createKeeper,
  startDive,
  stepKeeper,
  type DiveDirection,
  type KeeperState,
} from "../entities/keeper";
import {
  currentShotLane,
  isShotArrived,
  pickShotProfile,
  resetShotIdCounter,
  spawnShot,
  stepShot,
  type ShotState,
} from "../entities/shot";
import {
  activatePowerup,
  applyPowerupTick,
  hasActivePowerup,
  isPowerupExpired,
  resetPowerupIdCounter,
  spawnPowerup,
  type ActivePowerup,
  type PowerupState,
} from "../entities/powerup";
import {
  isParticleExpired,
  resetParticleIdCounter,
  spawnParticles,
  stepParticle,
  type ParticleState,
} from "../entities/particle";
import { getModeConfig } from "../modes";

export interface SimulationConfig {
  mode: GameMode;
  /** Seed as provided by the server at session open (string or number). */
  seed: string | number;
}

export type SimInputAction =
  | { type: "dive"; direction: DiveDirection }
  | { type: "moveTo"; x: number }
  | { type: "none" };

export interface SimulationState {
  mode: GameMode;
  seed: number;
  nowMs: number;
  elapsedMs: number;
  remainingMs: number | null; // null = untimed (e.g. sudden death/shootout by lives)
  livesRemaining: number | null; // null = not life-based
  score: number;
  combo: number;
  comboMultiplier: number;
  keeper: KeeperState;
  shots: ShotState[];
  powerups: PowerupState[];
  activePowerups: ActivePowerup[];
  particles: ParticleState[];
  nextShotAtMs: number;
  nextPowerupAtMs: number;
  summary: EventSummary;
  isOver: boolean;
  shotsRemaining: number | null; // for shootout mode
}

/**
 * Deterministic simulation core: zero React/network imports. Given the same
 * seed and the same ordered stream of input actions + step calls, produces
 * an identical trajectory every time. This is the seam that allows a future
 * "replay" feature driven purely by (seed, action log).
 */
export class Simulation {
  private rng: Rng;
  public state: SimulationState;

  constructor(config: SimulationConfig) {
    resetShotIdCounter();
    resetPowerupIdCounter();
    resetParticleIdCounter();

    const seed = normalizeSeed(config.seed);
    this.rng = new Rng(seed);
    const modeConfig = getModeConfig(config.mode);

    this.state = {
      mode: config.mode,
      seed,
      nowMs: 0,
      elapsedMs: 0,
      remainingMs: modeConfig.durationMs,
      livesRemaining: modeConfig.lives,
      score: 0,
      combo: 0,
      comboMultiplier: 1,
      keeper: createKeeper(),
      shots: [],
      powerups: [],
      activePowerups: [],
      particles: [],
      nextShotAtMs: modeConfig.initialShotDelayMs,
      nextPowerupAtMs: modeConfig.initialPowerupDelayMs,
      summary: createEmptyEventSummary(),
      isOver: false,
      shotsRemaining: modeConfig.shotLimit,
    };
  }

  applyAction(action: SimInputAction): void {
    if (this.state.isOver) return;
    if (action.type === "dive") {
      this.state.keeper = startDive(this.state.keeper, action.direction);
    } else if (action.type === "moveTo") {
      this.state.keeper = {
        ...this.state.keeper,
        x: Math.min(1, Math.max(0, action.x)),
      };
      this.collectPowerupsNearKeeper();
    }
  }

  /** Advances simulation by exactly one fixed timestep. */
  step(fixedDtMs: number): void {
    if (this.state.isOver) return;

    const modeConfig = getModeConfig(this.state.mode);
    const speedMultiplier = hasActivePowerup(this.state.activePowerups, "slowMotion")
      ? 0.55
      : 1;
    const effectiveDt = fixedDtMs * speedMultiplier;

    this.state.nowMs += fixedDtMs;
    this.state.elapsedMs += fixedDtMs;

    if (this.state.remainingMs !== null) {
      this.state.remainingMs = Math.max(0, this.state.remainingMs - fixedDtMs);
    }

    this.state.keeper = stepKeeper(this.state.keeper, fixedDtMs);
    this.state.keeper.diveReachExtra = hasActivePowerup(
      this.state.activePowerups,
      "wideArms",
    )
      ? 0.1
      : 0;

    this.state.activePowerups = applyPowerupTick(this.state.activePowerups, fixedDtMs);

    this.stepShots(effectiveDt);
    this.stepPowerupSpawns(fixedDtMs);
    this.stepParticles(fixedDtMs);

    this.maybeSpawnShot(fixedDtMs, modeConfig);
    this.checkGameOver(modeConfig);
  }

  private stepShots(effectiveDt: number): void {
    const nextShots: ShotState[] = [];
    for (let shot of this.state.shots) {
      shot = stepShot(shot, effectiveDt);
      if (!shot.resolved && isShotArrived(shot)) {
        shot = this.resolveShot(shot);
      }
      // Resolved shots are scored immediately in resolveShot(); they don't
      // need to linger in the active list for any further rendering frames.
      if (!shot.resolved) {
        nextShots.push(shot);
      }
    }
    this.state.shots = nextShots;
  }

  private resolveShot(shot: ShotState): ShotState {
    const lane = currentShotLane(shot);
    const saved = canReach(this.state.keeper, lane);
    const perfect =
      saved && Math.abs(this.state.keeper.x - lane) <= 0.05 && this.state.keeper.diving;

    this.state.summary.shotsFaced += 1;

    if (saved) {
      this.state.summary.saves += 1;
      this.state.combo += 1;
      this.state.summary.maxCombo = Math.max(
        this.state.summary.maxCombo,
        this.state.combo,
      );
      this.state.comboMultiplier = 1 + Math.floor(this.state.combo / 3) * 0.5;

      let points = this.pointsForShot(shot.kind) * this.state.comboMultiplier;
      if (perfect) {
        this.state.summary.perfectSaves += 1;
        points *= 1.5;
      }
      if (hasActivePowerup(this.state.activePowerups, "doublePoints")) {
        points *= 2;
      }
      this.state.score += Math.round(points);

      this.state.particles.push(
        ...spawnParticles(
          this.rng,
          perfect ? "comboSparkle" : "saveBurst",
          { x: lane, y: 0.85 },
          perfect ? 14 : 8,
        ),
      );

      return { ...shot, resolved: true, outcome: perfect ? "perfectSave" : "saved" };
    }

    this.state.summary.goalsConceded += 1;
    this.state.combo = 0;
    this.state.comboMultiplier = 1;
    if (this.state.livesRemaining !== null) {
      this.state.livesRemaining = Math.max(0, this.state.livesRemaining - 1);
    }
    this.state.particles.push(
      ...spawnParticles(this.rng, "goalFlash", { x: lane, y: 1 }, 10),
    );

    return { ...shot, resolved: true, outcome: "goal" };
  }

  private pointsForShot(kind: ShotState["kind"]): number {
    switch (kind) {
      case "ground":
        return 10;
      case "header":
        return 15;
      case "fake":
        return 20;
      case "deflection":
        return 18;
      case "rocket":
        return 25;
    }
  }

  private stepPowerupSpawns(fixedDtMs: number): void {
    this.state.powerups = this.state.powerups.filter(
      (p) => !isPowerupExpired(p, this.state.nowMs),
    );

    if (this.state.nowMs >= this.state.nextPowerupAtMs) {
      this.state.powerups.push(spawnPowerup(this.rng, this.state.nowMs));
      this.state.nextPowerupAtMs =
        this.state.nowMs + this.rng.nextRange(6000, 12000);
    }
    void fixedDtMs;
  }

  private stepParticles(fixedDtMs: number): void {
    this.state.particles = this.state.particles
      .map((p) => stepParticle(p, fixedDtMs))
      .filter((p) => !isParticleExpired(p));
  }

  /** Player attempts to collect a powerup at the keeper's current lane. */
  collectPowerupsNearKeeper(): void {
    const reachX = 0.08;
    const remaining: PowerupState[] = [];
    for (const powerup of this.state.powerups) {
      if (!powerup.collected && Math.abs(powerup.x - this.state.keeper.x) <= reachX) {
        this.state.activePowerups = activatePowerup(
          this.state.activePowerups,
          powerup.kind,
        );
      } else {
        remaining.push(powerup);
      }
    }
    this.state.powerups = remaining;
  }

  private maybeSpawnShot(
    fixedDtMs: number,
    modeConfig: ReturnType<typeof getModeConfig>,
  ): void {
    if (this.state.shotsRemaining !== null && this.state.shotsRemaining <= 0) return;
    if (this.state.nowMs < this.state.nextShotAtMs) return;

    const profile = pickShotProfile(this.rng);
    const difficultyBoost = 1 + Math.min(this.state.elapsedMs / 60000, 1);
    this.state.shots.push(spawnShot(this.rng, profile, difficultyBoost));

    if (this.state.shotsRemaining !== null) {
      this.state.shotsRemaining -= 1;
    }

    const baseGap = modeConfig.shotIntervalMs;
    const minGap = baseGap * 0.5;
    const gap = Math.max(minGap, baseGap - this.state.elapsedMs / 100);
    this.state.nextShotAtMs = this.state.nowMs + gap;
    void fixedDtMs;
  }

  private checkGameOver(modeConfig: ReturnType<typeof getModeConfig>): void {
    if (this.state.remainingMs !== null && this.state.remainingMs <= 0) {
      this.state.isOver = true;
      return;
    }
    if (this.state.livesRemaining !== null && this.state.livesRemaining <= 0) {
      this.state.isOver = true;
      return;
    }
    if (
      this.state.shotsRemaining !== null &&
      this.state.shotsRemaining <= 0 &&
      this.state.shots.length === 0
    ) {
      this.state.isOver = true;
      return;
    }
    void modeConfig;
  }
}
