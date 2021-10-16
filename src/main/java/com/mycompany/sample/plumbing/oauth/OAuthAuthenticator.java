package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.util.HashSet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.claims.ClaimParser;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
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
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;

/*
 * The entry point for calls to the Authorization Server
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthAuthenticator {

    private final OAuthConfiguration configuration;
    private final RemoteJWKSet<SecurityContext> jwksKeys;
    private final LogEntry logEntry;

    public OAuthAuthenticator(
            final OAuthConfiguration configuration,
            final RemoteJWKSet<SecurityContext> jwksKeys,
            final LogEntry logEntry) {

        this.configuration = configuration;
        this.jwksKeys = jwksKeys;
        this.logEntry = logEntry;
    }

    /*
     * Do the work of validating the access token and returning its claims
     */
    public JWTClaimsSet validateToken(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {

            // First get the access token header's kid value
            var jwt = this.decodeAccessToken(accessToken);
            var kid = jwt.getHeader().getKeyID();

            // Download the token signing public key
            var publicKey = this.getTokenSigningPublicKey(kid);

            // Verify the token's digital signature and get its claims
            return this.validateJsonWebToken(jwt, publicKey);
        }
    }

    /*
     * Perform OAuth user info lookup
     */
    public JWTClaimsSet getUserInfo(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Make the request
            var userInfoUrl = new URI(this.configuration.getUserInfoEndpoint());
            HTTPResponse httpResponse = new UserInfoRequest(userInfoUrl, new BearerAccessToken(accessToken))
                    .toHTTPRequest()
                    .send();

            // Handle errors returned in the response body
            var userInfoResponse = UserInfoResponse.parse(httpResponse);
            if (!userInfoResponse.indicatesSuccess()) {
                var errorResponse = UserInfoErrorResponse.parse(httpResponse);
                throw ErrorUtils.fromUserInfoError(
                        errorResponse.getErrorObject(),
                        this.configuration.getUserInfoEndpoint());
            }

            // Get claims from the response
            var data = userInfoResponse.toSuccessResponse().getUserInfo();

            // Return a claims set object with results
            return new JWTClaimsSet.Builder()
                    .claim("given_name", ClaimParser.getStringClaim(data, "given_name"))
                    .claim("family_name", ClaimParser.getStringClaim(data, "family_name"))
                    .claim("email", ClaimParser.getStringClaim(data, "email"))
                    .build();

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromUserInfoError(e, this.configuration.getUserInfoEndpoint());
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
            this.createClaimsVerifier().verify(jwtClaims, null);
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
