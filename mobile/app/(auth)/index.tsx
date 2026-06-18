import React, { useEffect, useState } from "react";
import { Alert, StyleSheet, Text, View } from "react-native";
import * as WebBrowser from "expo-web-browser";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "@/state/AuthContext";
import { Button } from "@/components/Button";
import { colors } from "@/theme/colors";
import {
  useGoogleIdTokenRequest,
  isGoogleConfigured,
  useFacebookIdTokenRequest,
  isFacebookConfigured,
  signInWithApple,
  isAppleSignInAvailable,
} from "@/net/idp";

WebBrowser.maybeCompleteAuthSession();

export default function AuthScreen() {
  const insets = useSafeAreaInsets();
  const { signInWithIdToken, signInDev } = useAuth();
  const [busyProvider, setBusyProvider] = useState<string | null>(null);
  const [appleAvailable, setAppleAvailable] = useState(false);

  const [googleRequest, googleResponse, promptGoogle] = useGoogleIdTokenRequest();
  const [facebookRequest, facebookResponse, promptFacebook] = useFacebookIdTokenRequest();

  useEffect(() => {
    isAppleSignInAvailable().then(setAppleAvailable).catch(() => setAppleAvailable(false));
  }, []);

  useEffect(() => {
    if (googleResponse?.type === "success" && googleResponse.params.id_token) {
      void completeSignIn("google", googleResponse.params.id_token);
    } else if (googleResponse?.type === "error") {
      Alert.alert("Google sign-in failed", googleResponse.error?.message ?? "Unknown error");
      setBusyProvider(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [googleResponse]);

  useEffect(() => {
    if (facebookResponse?.type === "success" && facebookResponse.params.id_token) {
      void completeSignIn("facebook", facebookResponse.params.id_token);
    } else if (facebookResponse?.type === "error") {
      Alert.alert("Facebook sign-in failed", facebookResponse.error?.message ?? "Unknown error");
      setBusyProvider(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [facebookResponse]);

  async function completeSignIn(provider: "google" | "facebook" | "apple", idToken: string) {
    try {
      await signInWithIdToken(provider, idToken);
    } catch (error) {
      Alert.alert("Sign-in failed", error instanceof Error ? error.message : "Unknown error");
    } finally {
      setBusyProvider(null);
    }
  }

  async function handleGoogle() {
    setBusyProvider("google");
    try {
      await promptGoogle();
    } catch (error) {
      Alert.alert("Google sign-in failed", error instanceof Error ? error.message : "Unknown error");
      setBusyProvider(null);
    }
  }

  async function handleFacebook() {
    setBusyProvider("facebook");
    try {
      await promptFacebook();
    } catch (error) {
      Alert.alert("Facebook sign-in failed", error instanceof Error ? error.message : "Unknown error");
      setBusyProvider(null);
    }
  }

  async function handleApple() {
    setBusyProvider("apple");
    try {
      const result = await signInWithApple();
      await completeSignIn("apple", result.identityToken);
    } catch (error) {
      setBusyProvider(null);
      const message = error instanceof Error ? error.message : "Unknown error";
      if (!message.includes("ERR_REQUEST_CANCELED")) {
        Alert.alert("Apple sign-in failed", message);
      }
    }
  }

  async function handleDevLogin() {
    setBusyProvider("dev");
    try {
      await signInDev();
    } catch (error) {
      Alert.alert("Dev sign-in failed", error instanceof Error ? error.message : "Unknown error");
    } finally {
      setBusyProvider(null);
    }
  }

  return (
    <View style={[styles.container, { paddingTop: insets.top + 48, paddingBottom: insets.bottom + 24 }]}>
      <View style={styles.hero}>
        <Text style={styles.title}>GoalKeeper Dash</Text>
        <Text style={styles.subtitle}>Defend your nation&apos;s honor, one save at a time.</Text>
      </View>

      <View style={styles.buttons}>
        <Button
          label="Continue with Google"
          onPress={handleGoogle}
          loading={busyProvider === "google"}
          disabled={!googleRequest || busyProvider !== null}
        />
        <Button
          label="Continue with Facebook"
          onPress={handleFacebook}
          variant="secondary"
          loading={busyProvider === "facebook"}
          disabled={!facebookRequest || busyProvider !== null}
        />
        {appleAvailable && (
          <Button
            label="Continue with Apple"
            onPress={handleApple}
            variant="secondary"
            loading={busyProvider === "apple"}
            disabled={busyProvider !== null}
          />
        )}

        {(!isGoogleConfigured() || !isFacebookConfigured()) && (
          <Button
            label="Dev login (no IdP configured)"
            onPress={handleDevLogin}
            variant="outline"
            loading={busyProvider === "dev"}
            disabled={busyProvider !== null}
          />
        )}
      </View>

      <Text style={styles.footnote}>
        By continuing you agree this is a casual arcade game and your score contributes to
        your nation&apos;s global total.
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 24,
    justifyContent: "space-between",
  },
  hero: {
    gap: 8,
    alignItems: "center",
  },
  title: {
    fontSize: 32,
    fontWeight: "800",
    color: colors.text,
    textAlign: "center",
  },
  subtitle: {
    fontSize: 15,
    color: colors.textMuted,
    textAlign: "center",
  },
  buttons: {
    gap: 12,
  },
  footnote: {
    fontSize: 11,
    color: colors.textMuted,
    textAlign: "center",
  },
});
