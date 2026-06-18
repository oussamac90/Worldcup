import { httpClient } from "../httpClient";
import { tokenStorage } from "../tokenStorage";
import type { AuthProvider, AuthResponse, RefreshResponse } from "../types";

export const authApi = {
  async signIn(provider: Exclude<AuthProvider, "dev">, idToken: string): Promise<AuthResponse> {
    const response = await httpClient.request<AuthResponse>(`/auth/${provider}`, {
      method: "POST",
      body: { idToken },
      auth: false,
    });
    await tokenStorage.save(response);
    return response;
  },

  /** Dev-only stub login path, for environments without real IdP credentials configured. */
  async devSignIn(idToken = "dev-token"): Promise<AuthResponse> {
    const response = await httpClient.request<AuthResponse>("/auth/dev", {
      method: "POST",
      body: { idToken },
      auth: false,
    });
    await tokenStorage.save(response);
    return response;
  },

  async refresh(refreshToken: string): Promise<RefreshResponse> {
    const response = await httpClient.request<RefreshResponse>("/auth/refresh", {
      method: "POST",
      body: { refreshToken },
      auth: false,
    });
    await tokenStorage.save(response);
    return response;
  },

  async logout(refreshToken: string): Promise<void> {
    try {
      await httpClient.request<void>("/auth/logout", {
        method: "POST",
        body: { refreshToken },
        auth: false,
      });
    } finally {
      await tokenStorage.clear();
    }
  },
};
