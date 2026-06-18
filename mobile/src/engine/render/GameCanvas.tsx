import React, { useCallback, useRef, useState } from "react";
import { StyleSheet, View } from "react-native";
import { Gesture, GestureDetector } from "react-native-gesture-handler";
import type { GameMode } from "../core/types";
import type { Simulation } from "../core/simulation";
import { useGameLoop } from "./useGameLoop";
import { GoalField, type FieldLayout } from "./GoalField";
import { KeeperSprite } from "./KeeperSprite";
import { ShotSprite } from "./ShotSprite";
import { PowerupSprite } from "./PowerupSprite";
import { ParticleField } from "./ParticleField";
import { Hud } from "./Hud";
import type { DiveDirection } from "../entities/keeper";

export interface GameCanvasProps {
  mode: GameMode;
  seed: string | number;
  paused?: boolean;
  onGameOver: (sim: Simulation) => void;
}

/**
 * Top-level render component for a play session. Reads simulation state via
 * `useGameLoop` every frame and renders presentational sprites — it never
 * mutates simulation internals directly except by dispatching input actions.
 */
export function GameCanvas({ mode, seed, paused = false, onGameOver }: GameCanvasProps) {
  const [field, setField] = useState<FieldLayout>({ width: 0, height: 0 });
  const handledOverRef = useRef(false);

  const { simulation, dispatch } = useGameLoop({
    mode,
    seed,
    active: !paused,
    onGameOver: (sim) => {
      if (handledOverRef.current) return;
      handledOverRef.current = true;
      onGameOver(sim);
    },
  });

  const handleLayout = useCallback((layout: FieldLayout) => {
    setField(layout);
  }, []);

  const moveKeeperTo = useCallback(
    (normalizedX: number) => {
      dispatch({ type: "moveTo", x: normalizedX });
    },
    [dispatch],
  );

  const diveDirectionFor = (normalizedX: number): DiveDirection => {
    if (normalizedX < simulation.state.keeper.x - 0.03) return "left";
    if (normalizedX > simulation.state.keeper.x + 0.03) return "right";
    return "stay";
  };

  const panGesture = Gesture.Pan()
    .onUpdate((event) => {
      if (field.width <= 0) return;
      const normalizedX = event.x / field.width;
      moveKeeperTo(normalizedX);
    })
    .runOnJS(true);

  const tapGesture = Gesture.Tap()
    .onStart((event) => {
      if (field.width <= 0) return;
      const normalizedX = event.x / field.width;
      const direction = diveDirectionFor(normalizedX);
      moveKeeperTo(normalizedX);
      dispatch({ type: "dive", direction });
    })
    .runOnJS(true);

  const composedGesture = Gesture.Simultaneous(panGesture, tapGesture);

  const { state } = simulation;

  return (
    <GestureDetector gesture={composedGesture}>
      <View style={styles.container}>
        <GoalField onLayout={handleLayout}>
          {field.width > 0 && (
            <>
              {state.shots.map((shot) => (
                <ShotSprite key={shot.id} shot={shot} field={field} />
              ))}
              {state.powerups.map((powerup) => (
                <PowerupSprite key={powerup.id} powerup={powerup} field={field} />
              ))}
              <KeeperSprite keeper={state.keeper} field={field} />
              <ParticleField particles={state.particles} field={field} />
            </>
          )}
        </GoalField>
        <Hud state={state} />
      </View>
    </GestureDetector>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
});
