import "react-native-gesture-handler";
import React, { useEffect } from "react";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { Stack, useRouter, useSegments } from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import { StatusBar } from "expo-status-bar";
import { AuthProvider, useAuth } from "@/state/AuthContext";
import { colors } from "@/theme/colors";

SplashScreen.preventAutoHideAsync().catch(() => undefined);

function RootNavigator() {
  const { status, me } = useAuth();
  const router = useRouter();
  const segments = useSegments();

  useEffect(() => {
    if (status === "loading") return;

    SplashScreen.hideAsync().catch(() => undefined);

    const inAuthGroup = segments[0] === "(auth)";
    const inAppGroup = segments[0] === "(app)";
    const onNationPicker = segments[0] === "nation-picker";

    if (status === "signedOut" && !inAuthGroup) {
      router.replace("/(auth)");
      return;
    }

    if (status === "signedIn") {
      const needsNation = me ? me.nation === null : false;
      if (needsNation && !onNationPicker) {
        router.replace("/nation-picker");
        return;
      }
      if (!needsNation && (inAuthGroup || onNationPicker)) {
        router.replace("/(app)");
        return;
      }
      if (inAuthGroup && !inAppGroup) {
        router.replace("/(app)");
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status, me, segments.join("/")]);

  return (
    <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: colors.background } }}>
      <Stack.Screen name="(auth)" />
      <Stack.Screen name="nation-picker" options={{ presentation: "card" }} />
      <Stack.Screen name="(app)" />
    </Stack>
  );
}

export default function RootLayout() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <StatusBar style="light" />
          <RootNavigator />
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
