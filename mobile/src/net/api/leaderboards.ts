import { httpClient } from "../httpClient";
import type {
  GlobalUsersResponse,
  MyLeaderboardResponse,
  NationLeaderboardResponse,
  NationUsersResponse,
} from "../types";

export interface PaginationParams {
  limit?: number;
  offset?: number;
}

export const leaderboardsApi = {
  async me(): Promise<MyLeaderboardResponse> {
    return httpClient.request<MyLeaderboardResponse>("/leaderboards/me");
  },

  async nations(params: { seasonId?: string } & PaginationParams = {}): Promise<NationLeaderboardResponse> {
    return httpClient.request<NationLeaderboardResponse>("/leaderboards/nations", {
      query: { seasonId: params.seasonId, limit: params.limit, offset: params.offset },
    });
  },

  async nationUsers(
    code: string,
    params: PaginationParams = {},
  ): Promise<NationUsersResponse> {
    return httpClient.request<NationUsersResponse>(`/leaderboards/nations/${code}/users`, {
      query: { limit: params.limit, offset: params.offset },
    });
  },

  async users(params: PaginationParams = {}): Promise<GlobalUsersResponse> {
    return httpClient.request<GlobalUsersResponse>("/leaderboards/users", {
      query: { limit: params.limit, offset: params.offset },
    });
  },
};
