package com.goalkeeperdash.user.auth;

import com.goalkeeperdash.common.config.AppProperties;
import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.common.error.ErrorCode;
import com.goalkeeperdash.user.domain.AuthProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Verifies IdP ID tokens server-side (§4.1/§8.2): validates the signature against
 * the provider's JWKS, and checks {@code iss}, {@code aud} (your client IDs) and
 * {@code exp}. Never trusts an unverified token. JWKS are cached by Nimbus's
 * {@link RemoteJWKSet} with a sane TTL.
 */
@Service
public class OidcTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(OidcTokenVerifier.class);

    private record ProviderConfig(String issuer, String jwksUri, String audience) {}

    private final AppProperties props;
    private final Map<AuthProvider, ConfigurableJWTProcessor<SecurityContext>> processors = new ConcurrentHashMap<>();

    public OidcTokenVerifier(AppProperties props) {
        this.props = props;
    }

    public VerifiedIdentity verify(AuthProvider provider, String idToken) {
        ProviderConfig cfg = configFor(provider);
        if (!StringUtils.hasText(cfg.audience())) {
            throw new ApiException(ErrorCode.INVALID_TOKEN,
                    "Provider " + provider + " is not configured (missing client id)");
        }
        try {
            ConfigurableJWTProcessor<SecurityContext> processor =
                    processors.computeIfAbsent(provider, p -> buildProcessor(cfg));
            JWTClaimsSet claims = processor.process(idToken, null);

            // Issuer check (allow Google's two accepted issuers).
            String iss = claims.getIssuer();
            if (!issuerMatches(provider, cfg, iss)) {
                throw new ApiException(ErrorCode.INVALID_TOKEN, "Token issuer mismatch");
            }
            // Audience check.
            List<String> aud = claims.getAudience();
            if (aud == null || !aud.contains(cfg.audience())) {
                throw new ApiException(ErrorCode.INVALID_TOKEN, "Token audience mismatch");
            }
            // Expiry is enforced by the processor's default claims verifier.
            String subject = claims.getSubject();
            if (!StringUtils.hasText(subject)) {
                throw new ApiException(ErrorCode.INVALID_TOKEN, "Token missing subject");
            }
            String email = (String) claims.getClaim("email");
            return new VerifiedIdentity(provider, subject, email);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("IdP token verification failed for {}: {}", provider, e.getMessage());
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Could not verify " + provider + " token");
        }
    }

    private boolean issuerMatches(AuthProvider provider, ProviderConfig cfg, String iss) {
        if (iss == null) return false;
        if (provider == AuthProvider.GOOGLE) {
            return iss.equals("https://accounts.google.com") || iss.equals("accounts.google.com");
        }
        return iss.equals(cfg.issuer());
    }

    private ConfigurableJWTProcessor<SecurityContext> buildProcessor(ProviderConfig cfg) {
        try {
            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(cfg.jwksUri()));
            JWSKeySelector<SecurityContext> keySelector =
                    new JWSVerificationKeySelector<>(Set.of(JWSAlgorithm.RS256, JWSAlgorithm.ES256), keySource);
            processor.setJWSKeySelector(keySelector);
            return processor;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build JWT processor for " + cfg.issuer(), e);
        }
    }

    private ProviderConfig configFor(AuthProvider provider) {
        return switch (provider) {
            case GOOGLE -> new ProviderConfig(
                    "https://accounts.google.com",
                    "https://www.googleapis.com/oauth2/v3/certs",
                    props.oidc().googleClientId());
            case APPLE -> new ProviderConfig(
                    "https://appleid.apple.com",
                    "https://appleid.apple.com/auth/keys",
                    props.oidc().appleClientId());
            case FACEBOOK -> new ProviderConfig(
                    "https://www.facebook.com",
                    "https://www.facebook.com/.well-known/oauth/openid/jwks/",
                    props.oidc().facebookAppId());
            default -> throw new ApiException(ErrorCode.INVALID_TOKEN, "Unsupported provider: " + provider);
        };
    }
}
