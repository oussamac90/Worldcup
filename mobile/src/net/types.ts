import type { EventSummary, GameMode } from "@/engine/core/types";

export type AuthProvider = "google" | "facebook" | "apple" | "dev";

export interface AppUser {
  id: string;
  displayName: string;
  email: string | null;
  nationCode: string | null;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface AuthResponse extends AuthTokens {
  user: AppUser;
}

export interface RefreshResponse extends AuthTokens {}

export interface NationLockStatus {
  locked: boolean;
  seasonId: string;
  lockedAt: string | null;
}

export interface MeResponse {
  user: AppUser;
  nation: NationSummary | null;
  nationLock: NationLockStatus;
  currentSeason: SeasonSummary;
}

export interface SeasonSummary {
  id: string;
  name: string;
  startsAt: string;
  endsAt: string;
}

export interface NationSummary {
  code: string;
  name: string;
  flagColors: string[];
  active: boolean;
}

export interface CreateSessionRequest {
  mode: GameMode;
}

export interface CreateSessionResponse {
  sessionId: string;
  nonce: string;
  serverTime: string;
  seed: string;
}

export interface SubmitSessionRequest {
  nonce: string;
  score: number;
  mode: GameMode;
  durationMs: number;
  eventSummary: EventSummary;
}

export interface SubmitSessionResponse {
  accepted: boolean;
  score: number;
  personalBest: boolean;
}

export interface LeaderboardNeighbor {
  rank: number;
  displayName: string;
  score: number;
  isSelf: boolean;
}

export interface PersonalWithinNation {
  rank: number;
  total: number;
  bestScore: number;
  neighbors: LeaderboardNeighbor[];
}

export interface NationNeighbor {
  rank: number;
  code: string;
  name: string;
  totalScore: number;
  isSelf: boolean;
}

export interface NationInWorld {
  rank: number;
  totalNations: number;
  nationTotalScore: number;
  neighbors: NationNeighbor[];
}

export interface MyLeaderboardResponse {
  season: SeasonSummary;
  nation: NationSummary | null;
  personalWithinNation: PersonalWithinNation;
  nationInWorld: NationInWorld;
}

export interface NationLeaderboardEntry {
  rank: number;
  code: string;
  name: string;
  totalScore: number;
  memberCount: number;
}

export interface NationLeaderboardResponse {
  seasonId: string;
  entries: NationLeaderboardEntry[];
  limit: number;
  offset: number;
}

export interface NationUserEntry {
  rank: number;
  displayName: string;
  score: number;
}

export interface NationUsersResponse {
  nationCode: string;
  entries: NationUserEntry[];
  limit: number;
  offset: number;
}

export interface GlobalUserEntry {
  rank: number;
  displayName: string;
  nationCode: string | null;
  score: number;
}

export interface GlobalUsersResponse {
  entries: GlobalUserEntry[];
  limit: number;
  offset: number;
}
