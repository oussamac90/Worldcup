import React from "react";
import { StyleSheet, Text } from "react-native";
import Animated, { useAnimatedStyle, useDerivedValue } from "react-native-reanimated";
import type { PowerupKind, PowerupState } from "../entities/powerup";
import type { FieldLayout } from "./GoalField";

const POWERUP_ICON: Record<PowerupKind, string> = {
  slowMotion: "⏱",
  wideArms: "🤲",
  doublePoints: "2x",
  magnet: "🧲",
};

const POWERUP_COLOR: Record<PowerupKind, string> = {
  slowMotion: "#26C6DA",
  wideArms: "#AB47BC",
  doublePoints: "#FFD54F",
  magnet: "#8D6E63",
};

interface PowerupSpriteProps {
  powerup: PowerupState;
  field: FieldLayout;
}

export function PowerupSprite({ powerup, field }: PowerupSpriteProps) {
  const translateX = useDerivedValue(() => powerup.x * field.width - 18);
  const translateY = useDerivedValue(() => field.height * 0.45);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }, { translateY: translateY.value }],
  }));

  return (
    <Animated.View
      style={[
        styles.powerup,
        { backgroundColor: POWERUP_COLOR[powerup.kind] },
        animatedStyle,
      ]}
    >
      <Text style={styles.icon}>{POWERUP_ICON[powerup.kind]}</Text>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  powerup: {
    position: "absolute",
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderColor: "#FFFFFF",
  },
  icon: {
    fontSize: 16,
  },
});
