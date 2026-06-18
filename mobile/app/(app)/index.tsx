import React, { useCallback, useState } from "react";
import {
  ActivityIndicator,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useFocusEffect, useRouter } from "expo-router";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { Button } from "@/components/Button";
import { Card } from "@/components/Card";
import { RankPanel, type RankRow } from "@/components/RankPanel";
import { useAuth } from "@/state/AuthContext";
import { useLeaderboard } from "@/state/useLeaderboard";
import { colors } from "@/theme/colors";
import { listModes } from "@/engine/modes";
import type { GameMode } from "@/engine/core/types";

export default function HomeScreen() {
  const insets = useSafeAreaInsets();
  const router = useRouter();
  const { user, me, signOut } = useAuth();
  const { data, loading, error, refresh } = useLeaderboard();
  const [selectedMode, setSelectedMode] = useState<GameMode>("tournament");

  useFocusEffect(
    useCallback(() => {
      void refresh();
    }, [refresh]),
  );

  const personalRows: RankRow[] =
    data?.personalWithinNation.neighbors.map((n) => ({
      key: `${n.rank}-${n.displayName}`,
      rank: n.rank,
      label: n.displayName,
      value: n.score,
      isSelf: n.isSelf,
    })) ?? [];

  const nationRows: RankRow[] =
    data?.nationInWorld.neighbors.map((n) => ({
      key: `${n.rank}-${n.code}`,
      rank: n.rank,
      label: n.name,
      value: n.totalScore,
      isSelf: n.isSelf,
    })) ?? [];

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={[
        styles.content,
        { paddingTop: insets.top + 16, paddingBottom: insets.bottom + 24 },
      ]}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={refresh} tintColor={colors.primary} />}
    >
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Hi, {user?.displayName ?? "Keeper"}</Text>
          <Text style={styles.nation}>{me?.nation?.name ?? "No nation"}</Text>
        </View>
        <Button label="Sign out" onPress={signOut} variant="outline" style={styles.signOutButton} />
      </View>

      {error && (
        <Card>
          <Text style={styles.errorText}>{error}</Text>
        </Card>
      )}

      {!data && loading ? (
        <ActivityIndicator color={colors.primary} style={styles.loader} />
      ) : data ? (
        <>
          <RankPanel
            title="You, within your nation"
            subtitle={data.season.name}
            rank={data.personalWithinNation.rank}
            totalLabel={`of ${data.personalWithinNation.total} keepers · best ${data.personalWithinNation.bestScore.toLocaleString()}`}
            rows={personalRows}
          />
          <RankPanel
            title="Your nation, in the world"
            subtitle={data.nation?.name ?? undefined}
            rank={data.nationInWorld.rank}
            totalLabel={`of ${data.nationInWorld.totalNations} nations · ${data.nationInWorld.nationTotalScore.toLocaleString()} pts`}
            rows={nationRows}
          />
        </>
      ) : null}

      <Card style={styles.modeCard}>
        <Text style={styles.modeTitle}>Choose a mode</Text>
        <View style={styles.modeList}>
          {listModes().map((mode) => (
            <ModePill
              key={mode.mode}
              label={mode.label}
              selected={mode.mode === selectedMode}
              onPress={() => setSelectedMode(mode.mode)}
            />
          ))}
        </View>
        <Text style={styles.modeDescription}>
          {listModes().find((m) => m.mode === selectedMode)?.description}
        </Text>
      </Card>

      <Button
        label="Play"
        onPress={() => router.push({ pathname: "/(app)/play", params: { mode: selectedMode } })}
        style={styles.playButton}
      />
    </ScrollView>
  );
}

function ModePill({
  label,
  selected,
  onPress,
}: {
  label: string;
  selected: boolean;
  onPress: () => void;
}) {
  return (
    <Button
      label={label}
      onPress={onPress}
      variant={selected ? "primary" : "secondary"}
      style={styles.modePill}
    />
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingHorizontal: 16,
    gap: 16,
  },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-start",
  },
  greeting: {
    color: colors.text,
    fontSize: 22,
    fontWeight: "800",
  },
  nation: {
    color: colors.textMuted,
    fontSize: 13,
    marginTop: 2,
  },
  signOutButton: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  loader: {
    marginTop: 32,
  },
  errorText: {
    color: colors.danger,
  },
  modeCard: {
    gap: 10,
  },
  modeTitle: {
    color: colors.text,
    fontWeight: "700",
    fontSize: 15,
  },
  modeList: {
    flexDirection: "row",
    flexWrap: "wrap",
    gap: 8,
  },
  modePill: {
    paddingVertical: 8,
    paddingHorizontal: 12,
  },
  modeDescription: {
    color: colors.textMuted,
    fontSize: 12,
  },
  playButton: {
    marginTop: 4,
  },
});
