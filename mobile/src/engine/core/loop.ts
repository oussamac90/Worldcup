/**
 * Fixed-timestep game loop driver.
 *
 * The loop itself has no knowledge of React Native, timers, or rendering —
 * callers (typically a render-layer hook) push wall-clock deltas into
 * `advance`, and this accumulator decides how many fixed simulation steps
 * to run. This is what makes the simulation deterministic regardless of the
 * device's actual frame rate: the same sequence of fixed steps always
 * produces the same result for a given seed + input stream.
 */
export const FIXED_TIMESTEP_MS = 1000 / 60; // 60Hz simulation tick
const MAX_FRAME_DELTA_MS = 250; // clamp huge stalls (e.g. app backgrounded)
const MAX_STEPS_PER_ADVANCE = 8; // avoid spiral-of-death on slow devices

export interface FixedStepLoopOptions {
  /** Called once per fixed simulation tick with the fixed dt in ms. */
  onStep: (fixedDtMs: number, stepIndex: number) => void;
  /**
   * Called once per `advance()` call after all steps have run, with the
   * interpolation alpha (0..1) representing how far between the last and
   * next simulation tick we are — useful for smoothing rendering.
   */
  onAfterSteps?: (alpha: number) => void;
  timestepMs?: number;
}

export class FixedStepLoop {
  private accumulatorMs = 0;
  private stepIndex = 0;
  private readonly timestepMs: number;
  private readonly onStep: FixedStepLoopOptions["onStep"];
  private readonly onAfterSteps: FixedStepLoopOptions["onAfterSteps"];
  private running = false;

  constructor(options: FixedStepLoopOptions) {
    this.timestepMs = options.timestepMs ?? FIXED_TIMESTEP_MS;
    this.onStep = options.onStep;
    this.onAfterSteps = options.onAfterSteps;
  }

  start(): void {
    this.running = true;
    this.accumulatorMs = 0;
  }

  stop(): void {
    this.running = false;
  }

  get isRunning(): boolean {
    return this.running;
  }

  /** Feed wall-clock elapsed ms since last call. Runs zero or more fixed steps. */
  advance(rawDeltaMs: number): void {
    if (!this.running) return;
    const clamped = Math.min(Math.max(rawDeltaMs, 0), MAX_FRAME_DELTA_MS);
    this.accumulatorMs += clamped;

    let steps = 0;
    while (
      this.accumulatorMs >= this.timestepMs &&
      steps < MAX_STEPS_PER_ADVANCE
    ) {
      this.onStep(this.timestepMs, this.stepIndex);
      this.stepIndex += 1;
      this.accumulatorMs -= this.timestepMs;
      steps += 1;
    }

    if (this.onAfterSteps) {
      this.onAfterSteps(this.accumulatorMs / this.timestepMs);
    }
  }

  reset(): void {
    this.accumulatorMs = 0;
    this.stepIndex = 0;
  }
}
