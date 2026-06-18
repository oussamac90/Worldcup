import { httpClient } from "../httpClient";
import type {
  CreateSessionRequest,
  CreateSessionResponse,
  SubmitSessionRequest,
  SubmitSessionResponse,
} from "../types";

export const sessionsApi = {
  async create(request: CreateSessionRequest): Promise<CreateSessionResponse> {
    return httpClient.request<CreateSessionResponse>("/sessions", {
      method: "POST",
      body: request,
    });
  },

  async submit(
    sessionId: string,
    request: SubmitSessionRequest,
  ): Promise<SubmitSessionResponse> {
    return httpClient.request<SubmitSessionResponse>(`/sessions/${sessionId}/submit`, {
      method: "POST",
      body: request,
    });
  },
};
