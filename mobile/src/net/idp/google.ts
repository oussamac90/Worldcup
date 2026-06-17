import * as Google from "expo-auth-session/providers/google";
import { Platform } from "react-native";
import { IDP_CONFIG } from "../config";

/**
 * Wraps expo-auth-session's Google id-token flow. Returns a loaded request,
 * the latest auth response, and a `promptAsync` function — same shape as
 * the underlying hook so screens can call it directly from a button press.
 */
export function useGoogleIdTokenRequest() {
  return Google.useIdTokenAuthRequest({
    iosClientId: IDP_CONFIG.googleClientIdIos || undefined,
    androidClientId: IDP_CONFIG.googleClientIdAndroid || undefined,
    webClientId: IDP_CONFIG.googleClientIdWeb || undefined,
    selectAccount: true,
  });
}

export function isGoogleConfigured(): boolean {
  if (Platform.OS === "ios") return Boolean(IDP_CONFIG.googleClientIdIos);
  if (Platform.OS === "android") return Boolean(IDP_CONFIG.googleClientIdAndroid);
  return Boolean(IDP_CONFIG.googleClientIdWeb);
}
