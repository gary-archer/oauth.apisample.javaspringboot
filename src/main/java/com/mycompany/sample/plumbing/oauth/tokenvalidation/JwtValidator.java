package com.mycompany.sample.plumbing.oauth.tokenvalidation;

import java.net.URI;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * An implementation that validates access tokens as JWTs
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class JwtValidator implements TokenValidator {

    private final OAuthConfiguration configuration;

    public JwtValidator(final OAuthConfiguration configuration) {
        this.configuration = configuration;
    }

    /*
     * The entry point for in memory token validation
     */
    @Override
    public ClaimsPayload validateToken(String accessToken) {

        try {
            // First get the access token header's kid value
            var jwt = this.decodeAccessToken(accessToken);
            var kid = jwt.getHeader().getKeyID();

            // Download the token signing public key
            var publicKey = this.getTokenSigningPublicKey(kid);

            // Verify the token's digital signature and get its claims
            var tokenClaims = this.validateJsonWebToken(jwt, publicKey);
            return new ClaimsPayload(tokenClaims);

            /*
            // Get token claims and use the immutable user id as the subject claim
            var subject = this.getStringClaim(tokenClaims, "sub");
            var scopes = this.getStringClaim(tokenClaims, "scope").split(" ");
            var expiry = (int) tokenClaims.getExpirationTime().toInstant().getEpochSecond();

            // Update token claims
            return new BaseClaims(subject, scopes, expiry);*/

        } catch (Throwable e) {

            // Report exceptions
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

            // Download token signing keys
            var jwksUri = new URI(this.configuration.getJwksEndpoint());
            JWKSet keys = JWKSet.load(jwksUri.toURL());

            // Get the key that matches the JWT
            var publicKey = keys.getKeyByKeyId(keyIdentifier);
            if (!(publicKey instanceof RSAKey)) {

                // Fail if not found or the wrong type
                String message = String.format("Key with identifier: %s not found in JWKS download", keyIdentifier);
                throw ErrorFactory.createClient401Error(message);
            }

            // Return the result
            return publicKey.toPublicJWK();

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromTokenSigningKeysDownloadError(e, this.configuration.getJwksEndpoint());
        }
    }

    /*
     * Do the work of verifying the access token
     */
    private JWTClaimsSet validateJsonWebToken(final SignedJWT jwt, final JWK publicKey) {

        try {

            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            if (!jwt.verify(verifier)) {
                throw ErrorUtils.fromAccessTokenValidationError(null);
            }

            return jwt.getJWTClaimsSet();

        } catch (Throwable e) {

            // Report exceptions
            throw ErrorUtils.fromAccessTokenValidationError(e);
        }
    }
}
