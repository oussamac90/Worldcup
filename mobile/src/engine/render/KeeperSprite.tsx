import React, { useEffect } from "react";
import { StyleSheet } from "react-native";
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withTiming,
  Easing,
} from "react-native-reanimated";
import type { KeeperState } from "../entities/keeper";
import type { FieldLayout } from "./GoalField";

interface KeeperSpriteProps {
  keeper: KeeperState;
  field: FieldLayout;
}

const KEEPER_WIDTH = 44;
const KEEPER_HEIGHT = 56;

export function KeeperSprite({ keeper, field }: KeeperSpriteProps) {
  const translateX = useSharedValue(keeper.x * field.width);
  const diveLean = useSharedValue(0);

  useEffect(() => {
    translateX.value = withTiming(keeper.x * field.width, {
      duration: 90,
      easing: Easing.out(Easing.quad),
    });
  }, [keeper.x, field.width, translateX]);

  useEffect(() => {
    const target = keeper.diving
      ? keeper.diveDirection === "left"
        ? -1
        : keeper.diveDirection === "right"
          ? 1
          : 0
      : 0;
    diveLean.value = withTiming(target, { duration: 140 });
  }, [keeper.diving, keeper.diveDirection, diveLean]);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        { translateX: translateX.value - KEEPER_WIDTH / 2 },
        { rotateZ: `${diveLean.value * 35}deg` },
        { scaleY: keeper.diving ? 0.85 : 1 },
      ],
    };
  });

  return <Animated.View style={[styles.keeper, animatedStyle]} />;
}

const styles = StyleSheet.create({
  keeper: {
    position: "absolute",
    bottom: "26%",
    width: KEEPER_WIDTH,
    height: KEEPER_HEIGHT,
    backgroundColor: "#FFC107",
    borderRadius: 10,
    borderWidth: 2,
    borderColor: "#5D4037",
  },
});
