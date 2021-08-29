package com.mycompany.sample.plumbing.oauth.tokenvalidation;

import java.util.HashSet;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;

/*
 * An implementation that validates access tokens as JWTs
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class JwtValidator implements TokenValidator {

    private final OAuthConfiguration configuration;
    private final RemoteJWKSet<SecurityContext> jwksKeys;

    public JwtValidator(
            final OAuthConfiguration configuration,
            final RemoteJWKSet<SecurityContext> jwksKeys) {

        this.configuration = configuration;
        this.jwksKeys = jwksKeys;
    }

    /*
     * The entry point for in memory token validation
     */
    @Override
    public JWTClaimsSet validateToken(final String accessToken) {

        try {
            // First get the access token header's kid value
            var jwt = this.decodeAccessToken(accessToken);
            var kid = jwt.getHeader().getKeyID();

            // Download the token signing public key
            var publicKey = this.getTokenSigningPublicKey(kid);

            // Verify the token's digital signature and get its claims
            return this.validateJsonWebToken(jwt, publicKey);

        } catch (Throwable e) {

            throw ErrorUtils.fromIntrospectionError(e, this.configuration.getIntrospectEndpoint().toString());
        }
    }

    /*
     * Decode the JWT and get its key identifier
     */
    private SignedJWT decodeAccessToken(final String accessToken) {

        try {
            return SignedJWT.parse(accessToken);

        } catch (Throwable e) {

            throw ErrorUtils.fromAccessTokenDecodeError(e);
        }
    }

    /*
     * Get the public key with which our access token is signed
     */
    private JWK getTokenSigningPublicKey(final String keyIdentifier) {

        try {

            // Match on an RSA key with the supplied id
            var matcher = new JWKMatcher.Builder()
                    .keyType(KeyType.RSA)
                    .keyID(keyIdentifier)
                    .build();
            var selector = new JWKSelector(matcher);

            // Get the key from the cache or download it if needed
            var keys = this.jwksKeys.get(selector, new SimpleSecurityContext());
            if (keys.size() != 1) {
                String message = String.format("Key with identifier: %s not found in JWKS download", keyIdentifier);
                throw ErrorFactory.createClient401Error(message);
            }

            // Return the public key, which will be used to verify the signature
            return keys.get(0);

        } catch (Throwable e) {

            throw ErrorUtils.fromTokenSigningKeysDownloadError(e, this.configuration.getJwksEndpoint());
        }
    }

    /*
     * Do the work of verifying the access token
     */
    private JWTClaimsSet validateJsonWebToken(final SignedJWT jwt, final JWK publicKey) {

        try {

            // First verify the RSA signature and expiry
            JWSVerifier jwtVerifier = new RSASSAVerifier((RSAKey) publicKey);
            if (!jwt.verify(jwtVerifier)) {
                throw ErrorUtils.fromAccessTokenValidationError(null);
            }

            // Next check the issuer and audience
            var jwtClaims = jwt.getJWTClaimsSet();
            this.createClaimsVerifier().verify(jwtClaims);
            return jwtClaims;

        } catch (Throwable e) {

            throw ErrorUtils.fromAccessTokenValidationError(e);
        }
    }

    /*
     * Deal with verifying the token's issuer and audience
     */
    private DefaultJWTClaimsVerifier<SecurityContext> createClaimsVerifier() {

        var issuer = new JWTClaimsSet.Builder().issuer(this.configuration.getIssuer()).build();
        var audience =  this.configuration.getAudience();
        if (StringUtils.hasLength(audience)) {

            // If there is an audience claim configured then verify it
            return new DefaultJWTClaimsVerifier<>(audience, issuer, new HashSet<>());

        } else {

            // Otherwise only verify the issuer
            return new DefaultJWTClaimsVerifier<>(issuer, new HashSet<>());
        }
    }
}
