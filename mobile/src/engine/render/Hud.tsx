import React from "react";
import { StyleSheet, Text, View } from "react-native";
import type { SimulationState } from "../core/simulation";

interface HudProps {
  state: SimulationState;
}

export function Hud({ state }: HudProps) {
  return (
    <View style={styles.container} pointerEvents="none">
      <View style={styles.row}>
        <Text style={styles.score}>{state.score}</Text>
        {state.combo > 1 && <Text style={styles.combo}>x{state.comboMultiplier.toFixed(1)} combo</Text>}
      </View>
      <View style={styles.row}>
        {state.remainingMs !== null && (
          <Text style={styles.meta}>{Math.ceil(state.remainingMs / 1000)}s</Text>
        )}
        {state.livesRemaining !== null && (
          <Text style={styles.meta}>{"♥".repeat(state.livesRemaining)}</Text>
        )}
        {state.shotsRemaining !== null && (
          <Text style={styles.meta}>{state.shotsRemaining} left</Text>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: "absolute",
    top: 12,
    left: 12,
    right: 12,
    flexDirection: "column",
    gap: 4,
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
  },
  score: {
    fontSize: 32,
    fontWeight: "800",
    color: "#FFFFFF",
  },
  combo: {
    fontSize: 16,
    fontWeight: "700",
    color: "#FFD54F",
  },
  meta: {
    fontSize: 16,
    fontWeight: "600",
    color: "#FFFFFF",
  },
});
