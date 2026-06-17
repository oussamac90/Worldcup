import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { authApi, profileApi, tokenStorage, httpClient } from "@/net";
import type { AppUser, AuthProvider, MeResponse } from "@/net/types";

interface AuthContextValue {
  status: "loading" | "signedOut" | "signedIn";
  user: AppUser | null;
  me: MeResponse | null;
  signInWithIdToken: (provider: Exclude<AuthProvider, "dev">, idToken: string) => Promise<void>;
  signInDev: () => Promise<void>;
  signOut: () => Promise<void>;
  refreshMe: () => Promise<MeResponse | null>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [status, setStatus] = useState<AuthContextValue["status"]>("loading");
  const [user, setUser] = useState<AppUser | null>(null);
  const [me, setMe] = useState<MeResponse | null>(null);

  const refreshMe = useCallback(async (): Promise<MeResponse | null> => {
    try {
      const response = await profileApi.getMe();
      setMe(response);
      setUser(response.user);
      return response;
    } catch {
      return null;
    }
  }, []);

  const handleSessionExpired = useCallback(() => {
    setStatus("signedOut");
    setUser(null);
    setMe(null);
  }, []);

  useEffect(() => {
    httpClient.setSessionExpiredHandler(handleSessionExpired);
    return () => httpClient.setSessionExpiredHandler(null);
  }, [handleSessionExpired]);

  useEffect(() => {
    (async () => {
      const accessToken = await tokenStorage.getAccessToken();
      if (!accessToken) {
        setStatus("signedOut");
        return;
      }
      const response = await refreshMe();
      if (response) {
        setStatus("signedIn");
      } else {
        setStatus("signedOut");
      }
    })();
  }, [refreshMe]);

  const signInWithIdToken = useCallback(
    async (provider: Exclude<AuthProvider, "dev">, idToken: string) => {
      const response = await authApi.signIn(provider, idToken);
      setUser(response.user);
      setStatus("signedIn");
      await refreshMe();
    },
    [refreshMe],
  );

  const signInDev = useCallback(async () => {
    const response = await authApi.devSignIn();
    setUser(response.user);
    setStatus("signedIn");
    await refreshMe();
  }, [refreshMe]);

  const signOut = useCallback(async () => {
    const refreshToken = await tokenStorage.getRefreshToken();
    if (refreshToken) {
      await authApi.logout(refreshToken);
    } else {
      await tokenStorage.clear();
    }
    setUser(null);
    setMe(null);
    setStatus("signedOut");
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({ status, user, me, signInWithIdToken, signInDev, signOut, refreshMe }),
    [status, user, me, signInWithIdToken, signInDev, signOut, refreshMe],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
