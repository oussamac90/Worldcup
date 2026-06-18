import React from "react";
import { StyleSheet, View } from "react-native";
import type { ParticleState } from "../entities/particle";
import type { FieldLayout } from "./GoalField";

interface ParticleFieldProps {
  particles: readonly ParticleState[];
  field: FieldLayout;
}

const PARTICLE_COLOR: Record<ParticleState["kind"], string> = {
  saveBurst: "#90CAF9",
  goalFlash: "#EF5350",
  comboSparkle: "#FFD54F",
};

/** Lightweight, non-animated particle renderer (positions already advanced by sim). */
export function ParticleField({ particles, field }: ParticleFieldProps) {
  return (
    <View style={StyleSheet.absoluteFill} pointerEvents="none">
      {particles.map((particle) => {
        const opacity = Math.max(0, 1 - particle.ageMs / particle.lifetimeMs);
        return (
          <View
            key={particle.id}
            style={[
              styles.particle,
              {
                left: particle.position.x * field.width - 4,
                top: particle.position.y * field.height - 4,
                backgroundColor: PARTICLE_COLOR[particle.kind],
                opacity,
              },
            ]}
          />
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  particle: {
    position: "absolute",
    width: 8,
    height: 8,
    borderRadius: 4,
  },
});
