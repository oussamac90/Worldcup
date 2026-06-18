import React, { useCallback, useEffect, useRef, useState } from "react";
import { ActivityIndicator, Alert, StyleSheet, Text, View } from "react-native";
import { useLocalSearchParams, useRouter } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { GameCanvas } from "@/engine/render";
import type { Simulation } from "@/engine/core/simulation";
import type { GameMode } from "@/engine/core/types";
import { sessionsApi } from "@/net/api/sessions";
import { Button } from "@/components/Button";
import { colors } from "@/theme/colors";

type ScreenState = "openingSession" | "playing" | "submitting" | "result" | "error";

interface SessionInfo {
  sessionId: string;
  nonce: string;
  seed: string;
}

export default function PlayScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const params = useLocalSearchParams<{ mode?: string }>();
  const mode = (params.mode as GameMode) ?? "tournament";

  const [screenState, setScreenState] = useState<ScreenState>("openingSession");
  const [session, setSession] = useState<SessionInfo | null>(null);
  const [finalScore, setFinalScore] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const startedAtRef = useRef<number>(0);

  const openSession = useCallback(async () => {
    setScreenState("openingSession");
    setErrorMessage(null);
    try {
      const response = await sessionsApi.create({ mode });
      setSession({ sessionId: response.sessionId, nonce: response.nonce, seed: response.seed });
      startedAtRef.current = Date.now();
      setScreenState("playing");
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "Could not start session");
      setScreenState("error");
    }
  }, [mode]);

  useEffect(() => {
    void openSession();
  }, [openSession]);

  const handleGameOver = useCallback(
    async (sim: Simulation) => {
      if (!session) return;
      setScreenState("submitting");
      setFinalScore(sim.state.score);
      try {
        await sessionsApi.submit(session.sessionId, {
          nonce: session.nonce,
          score: sim.state.score,
          mode,
          durationMs: Date.now() - startedAtRef.current,
          eventSummary: sim.state.summary,
        });
      } catch (error) {
        Alert.alert(
          "Could not submit score",
          error instanceof Error ? error.message : "Unknown error",
        );
      } finally {
        setScreenState("result");
      }
    },
    [session, mode],
  );

  if (screenState === "openingSession") {
    return (
      <View style={[styles.center, { paddingTop: insets.top }]}>
        <ActivityIndicator color={colors.primary} size="large" />
        <Text style={styles.statusText}>Opening session…</Text>
      </View>
    );
  }

  if (screenState === "error") {
    return (
      <View style={[styles.center, { paddingTop: insets.top, paddingHorizontal: 24 }]}>
        <Text style={styles.errorText}>{errorMessage}</Text>
        <Button label="Retry" onPress={openSession} style={styles.retryButton} />
        <Button label="Back to home" onPress={() => router.back()} variant="outline" />
      </View>
    );
  }

  if (screenState === "result") {
    return (
      <View style={[styles.center, { paddingTop: insets.top, paddingHorizontal: 24 }]}>
        <Text style={styles.resultTitle}>Game over</Text>
        <Text style={styles.resultScore}>{finalScore ?? 0}</Text>
        <Button label="Back to home" onPress={() => router.replace("/(app)")} />
      </View>
    );
  }

  if (screenState === "submitting") {
    return (
      <View style={[styles.center, { paddingTop: insets.top }]}>
        <ActivityIndicator color={colors.primary} size="large" />
        <Text style={styles.statusText}>Submitting score…</Text>
      </View>
    );
  }

  if (!session) return null;

  return (
    <View style={styles.gameContainer}>
      <GameCanvas mode={mode} seed={session.seed} onGameOver={handleGameOver} />
    </View>
  );
}

const styles = StyleSheet.create({
  center: {
    flex: 1,
    backgroundColor: colors.background,
    alignItems: "center",
    justifyContent: "center",
    gap: 16,
  },
  gameContainer: {
    flex: 1,
    backgroundColor: colors.background,
  },
  statusText: {
    color: colors.textMuted,
    fontSize: 14,
  },
  errorText: {
    color: colors.danger,
    textAlign: "center",
  },
  retryButton: {
    marginTop: 8,
  },
  resultTitle: {
    color: colors.text,
    fontSize: 22,
    fontWeight: "700",
  },
  resultScore: {
    color: colors.primary,
    fontSize: 56,
    fontWeight: "900",
  },
});
