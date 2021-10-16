package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.utilities.ErrorResponseReader;
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
     * Perform OAuth user info lookup via a plain HTTP request
     */
    public UserInfoClaims getUserInfo(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Prepare headers for a user info request
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(accessToken);
            var entity = new HttpEntity<>("body", headers);

            // Send the request and get the response as text
            var userInfoUrl = new URI(this.configuration.getUserInfoEndpoint());
            var userInfoClient = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
            var response = userInfoClient.exchange(userInfoUrl, HttpMethod.POST, entity, String.class);

            // Check for a valid response
            if (response.getStatusCode() != HttpStatus.OK) {
                var errorData = ErrorResponseReader.tryReadJson(response.hasBody() ? response.getBody() : "");
                throw ErrorUtils.fromUserInfoError(
                        response.getStatusCode(),
                        errorData,
                        this.configuration.getUserInfoEndpoint());
            }

            // Parse the fields into an object
            var jsonText = response.getBody();
            var mapper = new ObjectMapper();
            var data = mapper.readValue(jsonText, ObjectNode.class);
            return ClaimsReader.userInfoClaims(data);

        } catch (HttpStatusCodeException e) {

            // Report exceptions where we have a response body
            var errorData = ErrorResponseReader.tryReadJson(e.getResponseBodyAsString());
            throw ErrorUtils.fromUserInfoError(
                    e.getStatusCode(),
                    errorData,
                    this.configuration.getUserInfoEndpoint());

        } catch (Throwable e) {

            // Report other exceptions
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

            // If there is an audience claim configured then include it
            return new DefaultJWTClaimsVerifier<>(audience, issuer, new HashSet<>());

        } else {

            // Cognito does not include an audience claim
            return new DefaultJWTClaimsVerifier<>(issuer, new HashSet<>());
        }
    }
}
