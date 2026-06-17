import { useCallback, useEffect, useState } from "react";
import { leaderboardsApi } from "@/net/api/leaderboards";
import type { MyLeaderboardResponse } from "@/net/types";

interface UseLeaderboardResult {
  data: MyLeaderboardResponse | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useLeaderboard(): UseLeaderboardResult {
  const [data, setData] = useState<MyLeaderboardResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await leaderboardsApi.me();
      setData(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load leaderboard");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  return { data, loading, error, refresh };
}
