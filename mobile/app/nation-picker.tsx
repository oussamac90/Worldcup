import React, { useCallback, useEffect, useState } from "react";
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useRouter } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { profileApi } from "@/net/api/profile";
import { ApiError } from "@/net/httpClient";
import type { NationSummary } from "@/net/types";
import { useAuth } from "@/state/AuthContext";
import { colors } from "@/theme/colors";

export default function NationPickerScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { refreshMe } = useAuth();

  const [nations, setNations] = useState<NationSummary[] | null>(null);
  const [selecting, setSelecting] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadNations = useCallback(async () => {
    try {
      const list = await profileApi.listNations();
      setNations(list.filter((n) => n.active));
    } catch {
      setError("Could not load nations. Pull to retry.");
    }
  }, []);

  useEffect(() => {
    void loadNations();
  }, [loadNations]);

  async function handleSelect(nationCode: string) {
    setSelecting(nationCode);
    try {
      await profileApi.setNation(nationCode);
      await refreshMe();
      router.replace("/(app)");
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        Alert.alert("Nation locked", "Your nation is already locked for this season.");
      } else {
        Alert.alert("Could not set nation", err instanceof Error ? err.message : "Unknown error");
      }
    } finally {
      setSelecting(null);
    }
  }

  if (!nations) {
    return (
      <View style={[styles.container, styles.center]}>
        {error ? <Text style={styles.error}>{error}</Text> : <ActivityIndicator color={colors.primary} />}
      </View>
    );
  }

  return (
    <View style={[styles.container, { paddingTop: insets.top + 24 }]}>
      <Text style={styles.title}>Pick your nation</Text>
      <Text style={styles.subtitle}>
        Choose carefully — this is locked for the rest of the season.
      </Text>
      <FlatList
        data={nations}
        keyExtractor={(item) => item.code}
        numColumns={2}
        columnWrapperStyle={styles.row}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <Pressable
            onPress={() => handleSelect(item.code)}
            disabled={selecting !== null}
            style={({ pressed }) => [
              styles.tile,
              pressed && styles.tilePressed,
              selecting === item.code && styles.tileSelecting,
            ]}
          >
            <View style={styles.flagStripes}>
              {item.flagColors.slice(0, 3).map((color, idx) => (
                <View key={idx} style={[styles.flagStripe, { backgroundColor: color }]} />
              ))}
            </View>
            <Text style={styles.nationName} numberOfLines={1}>
              {item.name}
            </Text>
            {selecting === item.code && <ActivityIndicator color={colors.primary} />}
          </Pressable>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
    paddingHorizontal: 16,
  },
  center: {
    alignItems: "center",
    justifyContent: "center",
  },
  title: {
    fontSize: 24,
    fontWeight: "800",
    color: colors.text,
  },
  subtitle: {
    fontSize: 13,
    color: colors.textMuted,
    marginTop: 4,
    marginBottom: 16,
  },
  list: {
    gap: 12,
    paddingBottom: 24,
  },
  row: {
    gap: 12,
  },
  tile: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: 14,
    gap: 8,
    borderWidth: 1,
    borderColor: colors.border,
  },
  tilePressed: {
    opacity: 0.8,
  },
  tileSelecting: {
    opacity: 0.6,
  },
  flagStripes: {
    flexDirection: "row",
    height: 32,
    borderRadius: 6,
    overflow: "hidden",
  },
  flagStripe: {
    flex: 1,
  },
  nationName: {
    color: colors.text,
    fontWeight: "600",
    fontSize: 14,
  },
  error: {
    color: colors.danger,
  },
});
