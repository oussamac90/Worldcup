import { httpClient } from "../httpClient";
import type { MeResponse, NationSummary } from "../types";

export const profileApi = {
  async getMe(): Promise<MeResponse> {
    return httpClient.request<MeResponse>("/me");
  },

  async updateDisplayName(displayName: string): Promise<MeResponse> {
    return httpClient.request<MeResponse>("/me", {
      method: "PATCH",
      body: { displayName },
    });
  },

  async setNation(nationCode: string): Promise<MeResponse> {
    return httpClient.request<MeResponse>("/me/nation", {
      method: "PUT",
      body: { nationCode },
    });
  },

  async listNations(): Promise<NationSummary[]> {
    return httpClient.request<NationSummary[]>("/nations", { auth: false });
  },
};
