import React from "react";
import { StyleSheet, View, type LayoutChangeEvent } from "react-native";

export interface FieldLayout {
  width: number;
  height: number;
}

interface GoalFieldProps {
  onLayout: (layout: FieldLayout) => void;
  children: React.ReactNode;
}

/** The static pitch/goal backdrop. Purely presentational, owns no game state. */
export function GoalField({ onLayout, children }: GoalFieldProps) {
  const handleLayout = (event: LayoutChangeEvent) => {
    const { width, height } = event.nativeEvent.layout;
    onLayout({ width, height });
  };

  return (
    <View style={styles.field} onLayout={handleLayout}>
      <View style={styles.crossbar} />
      <View style={[styles.post, styles.postLeft]} />
      <View style={[styles.post, styles.postRight]} />
      <View style={styles.goalLine} />
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  field: {
    flex: 1,
    backgroundColor: "#2E7D32",
    overflow: "hidden",
    position: "relative",
  },
  crossbar: {
    position: "absolute",
    top: 24,
    left: "8%",
    right: "8%",
    height: 8,
    backgroundColor: "#F5F5F5",
    borderRadius: 2,
  },
  post: {
    position: "absolute",
    top: 24,
    bottom: "30%",
    width: 8,
    backgroundColor: "#F5F5F5",
    borderRadius: 2,
  },
  postLeft: { left: "8%" },
  postRight: { right: "8%" },
  goalLine: {
    position: "absolute",
    left: "4%",
    right: "4%",
    bottom: "30%",
    height: 3,
    backgroundColor: "rgba(245,245,245,0.6)",
  },
});
