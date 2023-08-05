package com.mycompany.sample.plumbing.oauth;

import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import java.util.Arrays;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;

/*
 * The entry point for OAuth related logic
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class OAuthAuthenticator {

    private final OAuthConfiguration configuration;
    private final HttpsJwksVerificationKeyResolver jwksResolver;
    private final LogEntry logEntry;

    public OAuthAuthenticator(
            final OAuthConfiguration configuration,
            final HttpsJwksVerificationKeyResolver jwksResolver,
            final LogEntry logEntry) {

        this.configuration = configuration;
        this.jwksResolver = jwksResolver;
        this.logEntry = logEntry;
    }

    /*
     * Do the work of validating the access token and returning its claims
     */
    public JwtClaims validateToken(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("validateToken")) {

            var builder = new JwtConsumerBuilder()
                .setVerificationKeyResolver(this.jwksResolver)
                .setJwsAlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    AlgorithmIdentifiers.RSA_USING_SHA256
                )
                .setExpectedIssuer(this.configuration.getIssuer());

            // Allow for AWS Cognito, which does not include an audience claim in access tokens
            if (StringUtils.hasLength(this.configuration.getAudience())) {
                builder.setExpectedAudience(this.configuration.getAudience());
            }

            // Validate the token and get its claims
            var jwtConsumer = builder.build();
            var claims = jwtConsumer.processToClaims(accessToken);

            // The sample API requires the same scope for all endpoints, and it is enforced here
            var scopes = ClaimsReader.getStringClaim(claims, "scope").split(" ");
            var foundScope = Arrays.stream(scopes).filter(s -> s.contains("investments")).findFirst();
            if (!foundScope.isPresent()) {
                throw ErrorFactory.createClientError(
                        HttpStatus.FORBIDDEN,
                        ErrorCodes.INSUFFICIENT_SCOPE,
                        "Access to this API endpoint is forbidden");
            }

            return claims;

        } catch (InvalidJwtException ex) {

            // Report failures
            throw ErrorUtils.fromAccessTokenValidationError(ex, this.configuration.getJwksEndpoint());
        }
    }
}
