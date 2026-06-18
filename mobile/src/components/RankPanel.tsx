import React from "react";
import { StyleSheet, Text, View } from "react-native";
import { Card } from "./Card";
import { colors } from "@/theme/colors";

export interface RankRow {
  key: string;
  rank: number;
  label: string;
  value: number;
  isSelf?: boolean;
}

interface RankPanelProps {
  title: string;
  subtitle?: string;
  rank: number;
  totalLabel: string;
  rows: RankRow[];
}

export function RankPanel({ title, subtitle, rank, totalLabel, rows }: RankPanelProps) {
  return (
    <Card style={styles.card}>
      <View style={styles.header}>
        <Text style={styles.title}>{title}</Text>
        {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
      </View>
      <View style={styles.rankRow}>
        <Text style={styles.rankNumber}>#{rank}</Text>
        <Text style={styles.rankTotal}>{totalLabel}</Text>
      </View>
      <View style={styles.neighbors}>
        {rows.map((row) => (
          <View
            key={row.key}
            style={[styles.neighborRow, row.isSelf && styles.neighborRowSelf]}
          >
            <Text style={[styles.neighborRank, row.isSelf && styles.selfText]}>
              #{row.rank}
            </Text>
            <Text
              style={[styles.neighborLabel, row.isSelf && styles.selfText]}
              numberOfLines={1}
            >
              {row.label}
            </Text>
            <Text style={[styles.neighborValue, row.isSelf && styles.selfText]}>
              {row.value.toLocaleString()}
            </Text>
          </View>
        ))}
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    gap: 10,
  },
  header: {
    gap: 2,
  },
  title: {
    color: colors.text,
    fontSize: 16,
    fontWeight: "700",
  },
  subtitle: {
    color: colors.textMuted,
    fontSize: 12,
  },
  rankRow: {
    flexDirection: "row",
    alignItems: "baseline",
    gap: 8,
  },
  rankNumber: {
    color: colors.primary,
    fontSize: 30,
    fontWeight: "800",
  },
  rankTotal: {
    color: colors.textMuted,
    fontSize: 13,
  },
  neighbors: {
    gap: 4,
  },
  neighborRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
    paddingVertical: 4,
  },
  neighborRowSelf: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: 8,
    paddingHorizontal: 6,
  },
  neighborRank: {
    color: colors.textMuted,
    width: 32,
    fontSize: 12,
  },
  neighborLabel: {
    color: colors.text,
    flex: 1,
    fontSize: 13,
  },
  neighborValue: {
    color: colors.text,
    fontSize: 13,
    fontWeight: "600",
  },
  selfText: {
    color: colors.primary,
  },
});
