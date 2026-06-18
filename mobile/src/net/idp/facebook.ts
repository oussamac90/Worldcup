import * as AuthSession from "expo-auth-session";
import * as Crypto from "expo-crypto";
import { useMemo, useState } from "react";
import { IDP_CONFIG } from "../config";

/**
 * Facebook's "Limited Login" supports OpenID Connect id_tokens via its
 * standard OAuth dialog with `response_type=id_token`. expo-auth-session
 * doesn't ship a dedicated id-token provider for Facebook (unlike Google),
 * so this builds the request directly against Facebook's dialog endpoint.
 */
const FACEBOOK_AUTHORIZATION_ENDPOINT = "https://www.facebook.com/v19.0/dialog/oauth";

export function useFacebookIdTokenRequest() {
  const [nonce] = useState<string>(() => Crypto.randomUUID());

  const redirectUri = AuthSession.makeRedirectUri();

  const config: AuthSession.AuthRequestConfig = useMemo(
    () => ({
      clientId: IDP_CONFIG.facebookAppId,
      responseType: AuthSession.ResponseType.IdToken,
      scopes: ["openid", "public_profile"],
      redirectUri,
      extraParams: { nonce },
    }),
    [redirectUri, nonce],
  );

  const discovery: AuthSession.DiscoveryDocument = useMemo(
    () => ({ authorizationEndpoint: FACEBOOK_AUTHORIZATION_ENDPOINT }),
    [],
  );

  const [request, response, promptAsync] = AuthSession.useAuthRequest(config, discovery);

  return [request, response, promptAsync, nonce] as const;
}

export function isFacebookConfigured(): boolean {
  return Boolean(IDP_CONFIG.facebookAppId);
}
