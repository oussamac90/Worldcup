import React from "react";
import { StyleSheet } from "react-native";
import Animated, { useAnimatedStyle, useDerivedValue } from "react-native-reanimated";
import { currentShotLane, type ShotState } from "../entities/shot";
import type { FieldLayout } from "./GoalField";

interface ShotSpriteProps {
  shot: ShotState;
  field: FieldLayout;
}

const SHOT_COLORS: Record<ShotState["kind"], string> = {
  ground: "#FFFFFF",
  header: "#FFEB3B",
  fake: "#FF7043",
  deflection: "#29B6F6",
  rocket: "#E53935",
};

export function ShotSprite({ shot, field }: ShotSpriteProps) {
  const lane = currentShotLane(shot);

  const translateX = useDerivedValue(() => lane * field.width - 10);
  const translateY = useDerivedValue(() => {
    // progress 0 = far (near halfway line), 1 = at goal line (keeper area)
    const startY = field.height * 0.05;
    const endY = field.height * 0.66;
    return startY + (endY - startY) * shot.progress;
  });
  const scale = useDerivedValue(() => 0.5 + shot.progress * 0.7);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { translateX: translateX.value },
      { translateY: translateY.value },
      { scale: scale.value },
    ],
  }));

  return (
    <Animated.View
      style={[
        styles.shot,
        { backgroundColor: SHOT_COLORS[shot.kind] },
        animatedStyle,
      ]}
    />
  );
}

const styles = StyleSheet.create({
  shot: {
    position: "absolute",
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: "#000",
  },
});
