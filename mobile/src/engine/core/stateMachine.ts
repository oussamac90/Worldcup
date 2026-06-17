import type { GamePhase } from "./types";

type Transition = Partial<Record<GamePhase, readonly GamePhase[]>>;

/** Allowed transitions for the top-level game state machine. */
const TRANSITIONS: Transition = {
  menu: ["countdown"],
  countdown: ["playing", "menu"],
  playing: ["paused", "gameover"],
  paused: ["playing", "gameover", "menu"],
  gameover: ["menu", "countdown"],
};

export class GameStateMachine {
  private phase: GamePhase;
  private listeners = new Set<(phase: GamePhase, prev: GamePhase) => void>();

  constructor(initial: GamePhase = "menu") {
    this.phase = initial;
  }

  get current(): GamePhase {
    return this.phase;
  }

  canTransition(next: GamePhase): boolean {
    const allowed = TRANSITIONS[this.phase];
    return Boolean(allowed && allowed.includes(next));
  }

  transition(next: GamePhase): boolean {
    if (!this.canTransition(next)) return false;
    const prev = this.phase;
    this.phase = next;
    for (const listener of this.listeners) listener(next, prev);
    return true;
  }

  subscribe(listener: (phase: GamePhase, prev: GamePhase) => void): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }
}
