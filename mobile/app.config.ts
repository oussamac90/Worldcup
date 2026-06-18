import type { ExpoConfig } from "expo/config";

const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const config: ExpoConfig = {
  name: "GoalKeeper Dash",
  slug: "goalkeeper-dash",
  scheme: "goalkeeperdash",
  version: "1.0.0",
  orientation: "portrait",
  icon: "./assets/icon.png",
  userInterfaceStyle: "light",
  ios: {
    supportsTablet: true,
    bundleIdentifier: "com.goalkeeperdash.app",
    usesAppleSignIn: true,
  },
  android: {
    package: "com.goalkeeperdash.app",
    adaptiveIcon: {
      backgroundColor: "#0B3D2E",
      foregroundImage: "./assets/android-icon-foreground.png",
      backgroundImage: "./assets/android-icon-background.png",
      monochromeImage: "./assets/android-icon-monochrome.png",
    },
  },
  web: {
    favicon: "./assets/favicon.png",
    bundler: "metro",
  },
  plugins: [
    "expo-router",
    "expo-secure-store",
    "expo-apple-authentication",
    [
      "expo-splash-screen",
      {
        backgroundColor: "#0B3D2E",
      },
    ],
  ],
  extra: {
    apiBaseUrl: API_BASE_URL,
    googleClientIdIos: process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID_IOS ?? "",
    googleClientIdAndroid:
      process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID_ANDROID ?? "",
    googleClientIdWeb: process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID_WEB ?? "",
    facebookAppId: process.env.EXPO_PUBLIC_FACEBOOK_APP_ID ?? "",
    eas: {
      projectId: process.env.EXPO_PUBLIC_EAS_PROJECT_ID ?? undefined,
    },
  },
  experiments: {
    typedRoutes: true,
  },
};

export default config;
