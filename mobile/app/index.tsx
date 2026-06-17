import React from "react";
import { ActivityIndicator, StyleSheet, View } from "react-native";
import { colors } from "@/theme/colors";

/**
 * Root index route. The actual destination (auth / nation-picker / home) is
 * decided by `app/_layout.tsx`'s `RootNavigator`, which redirects as soon as
 * auth status resolves. This screen is only ever visible for an instant
 * while that decision is being made.
 */
export default function RootIndex() {
  return (
    <View style={styles.container}>
      <ActivityIndicator color={colors.primary} size="large" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    alignItems: "center",
    justifyContent: "center",
  },
});
