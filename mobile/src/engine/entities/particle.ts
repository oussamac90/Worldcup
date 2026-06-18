import type { Rng } from "../core/rng";
import type { Vector2 } from "../core/types";

export type ParticleKind = "saveBurst" | "goalFlash" | "comboSparkle";

export interface ParticleState {
  id: number;
  kind: ParticleKind;
  position: Vector2;
  velocity: Vector2;
  ageMs: number;
  lifetimeMs: number;
}

let nextParticleId = 1;

export function resetParticleIdCounter(): void {
  nextParticleId = 1;
}

export function spawnParticles(
  rng: Rng,
  kind: ParticleKind,
  origin: Vector2,
  count: number,
): ParticleState[] {
  const particles: ParticleState[] = [];
  for (let i = 0; i < count; i++) {
    const angle = rng.nextRange(0, Math.PI * 2);
    const speed = rng.nextRange(0.15, 0.6);
    particles.push({
      id: nextParticleId++,
      kind,
      position: { ...origin },
      velocity: { x: Math.cos(angle) * speed, y: Math.sin(angle) * speed },
      ageMs: 0,
      lifetimeMs: rng.nextRange(300, 700),
    });
  }
  return particles;
}

export function stepParticle(particle: ParticleState, dtMs: number): ParticleState {
  const dtSec = dtMs / 1000;
  return {
    ...particle,
    position: {
      x: particle.position.x + particle.velocity.x * dtSec,
      y: particle.position.y + particle.velocity.y * dtSec,
    },
    ageMs: particle.ageMs + dtMs,
  };
}

export function isParticleExpired(particle: ParticleState): boolean {
  return particle.ageMs >= particle.lifetimeMs;
}
