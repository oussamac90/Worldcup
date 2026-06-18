import { apiUrl } from "./config";
import { tokenStorage } from "./tokenStorage";
import type { AuthTokens, RefreshResponse } from "./types";

export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(status: number, message: string, body: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

type QueryValue = string | number | undefined;

interface RequestOptions {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  body?: unknown;
  auth?: boolean; // attach Authorization header; defaults to true
  query?: { [key: string]: QueryValue };
}

type Unauthenticated401Handler = () => void;

/**
 * Thin typed fetch wrapper. Owns the access/refresh-token dance: on a 401
 * from an authenticated call, it attempts exactly one silent refresh via
 * POST /auth/refresh, retries the original request once, and otherwise
 * surfaces the error and notifies `onSessionExpired` so the app can route
 * back to the auth screen.
 */
class HttpClient {
  private refreshPromise: Promise<AuthTokens | null> | null = null;
  private onSessionExpired: Unauthenticated401Handler | null = null;

  setSessionExpiredHandler(handler: Unauthenticated401Handler | null): void {
    this.onSessionExpired = handler;
  }

  private buildUrl(path: string, query?: RequestOptions["query"]): string {
    const url = apiUrl(path);
    if (!query) return url;
    const params = Object.entries(query)
      .filter(([, v]) => v !== undefined)
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`);
    return params.length > 0 ? `${url}?${params.join("&")}` : url;
  }

  private async rawRequest<T>(path: string, options: RequestOptions, token: string | null): Promise<T> {
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(this.buildUrl(path, options.query), {
      method: options.method ?? "GET",
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });

    const text = await response.text();
    const data = text ? safeJsonParse(text) : undefined;

    if (!response.ok) {
      throw new ApiError(response.status, describeError(data, response.status), data);
    }

    return data as T;
  }

  private async refreshTokens(): Promise<AuthTokens | null> {
    if (this.refreshPromise) return this.refreshPromise;

    this.refreshPromise = (async () => {
      const refreshToken = await tokenStorage.getRefreshToken();
      if (!refreshToken) return null;
      try {
        const response = await this.rawRequest<RefreshResponse>(
          "/auth/refresh",
          { method: "POST", body: { refreshToken }, auth: false },
          null,
        );
        await tokenStorage.save(response);
        return response;
      } catch {
        await tokenStorage.clear();
        return null;
      }
    })();

    try {
      return await this.refreshPromise;
    } finally {
      this.refreshPromise = null;
    }
  }

  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const requiresAuth = options.auth !== false;
    const token = requiresAuth ? await tokenStorage.getAccessToken() : null;

    try {
      return await this.rawRequest<T>(path, options, token);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401 && requiresAuth) {
        const refreshed = await this.refreshTokens();
        if (refreshed) {
          return await this.rawRequest<T>(path, options, refreshed.accessToken);
        }
        this.onSessionExpired?.();
      }
      throw error;
    }
  }
}

function safeJsonParse(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function describeError(body: unknown, status: number): string {
  if (body && typeof body === "object" && "message" in body) {
    const message = (body as { message?: unknown }).message;
    if (typeof message === "string") return message;
  }
  return `Request failed with status ${status}`;
}

export const httpClient = new HttpClient();
