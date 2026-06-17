/**
 * Deterministic, seedable pseudo-random number generator.
 *
 * Uses the mulberry32 algorithm: small, fast, and produces a good enough
 * distribution for gameplay purposes (shot placement, powerup spawns,
 * particle jitter, etc.) while being trivially serializable as a single
 * 32-bit integer state. This determinism is what lets a (seed, inputs)
 * pair be replayed bit-for-bit in the future.
 */
export class Rng {
  private state: number;
  public readonly seed: number;

  constructor(seed: number) {
    // Force into uint32 range.
    this.seed = seed >>> 0;
    this.state = this.seed;
  }

  /** Returns a float in [0, 1). */
  next(): number {
    this.state |= 0;
    this.state = (this.state + 0x6d2b79f5) | 0;
    let t = this.state;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  }

  /** Returns an integer in [min, max] inclusive. */
  nextInt(min: number, max: number): number {
    return Math.floor(this.next() * (max - min + 1)) + min;
  }

  /** Returns a float in [min, max). */
  nextRange(min: number, max: number): number {
    return this.next() * (max - min) + min;
  }

  /** Returns true with the given probability (0..1). */
  chance(probability: number): boolean {
    return this.next() < probability;
  }

  /** Picks a random element from a non-empty array. */
  pick<T>(items: readonly T[]): T {
    if (items.length === 0) {
      throw new Error("Rng.pick: cannot pick from an empty array");
    }
    return items[this.nextInt(0, items.length - 1)];
  }

  /** Serializes current internal state for debugging/snapshots. */
  getState(): number {
    return this.state;
  }
}

/** Derives a 32-bit numeric seed from an arbitrary string (e.g. server-issued seed). */
export function hashSeedString(input: string): number {
  let hash = 0x811c9dc5;
  for (let i = 0; i < input.length; i++) {
    hash ^= input.charCodeAt(i);
    hash = Math.imul(hash, 0x01000193);
  }
  return hash >>> 0;
}

/** Normalizes a server-provided seed (string or number) into a numeric seed. */
export function normalizeSeed(seed: string | number): number {
  return typeof seed === "number" ? seed >>> 0 : hashSeedString(seed);
}
