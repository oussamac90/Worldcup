import Constants from "expo-constants";

interface AppExtra {
  apiBaseUrl?: string;
  googleClientIdIos?: string;
  googleClientIdAndroid?: string;
  googleClientIdWeb?: string;
  facebookAppId?: string;
}

function getExtra(): AppExtra {
  return (Constants.expoConfig?.extra ?? {}) as AppExtra;
}

export const API_BASE_URL = getExtra().apiBaseUrl ?? "http://localhost:8080";
export const API_V1_PREFIX = "/api/v1";

export const IDP_CONFIG = {
  googleClientIdIos: getExtra().googleClientIdIos ?? "",
  googleClientIdAndroid: getExtra().googleClientIdAndroid ?? "",
  googleClientIdWeb: getExtra().googleClientIdWeb ?? "",
  facebookAppId: getExtra().facebookAppId ?? "",
};

export function apiUrl(path: string): string {
  return `${API_BASE_URL}${API_V1_PREFIX}${path}`;
}
