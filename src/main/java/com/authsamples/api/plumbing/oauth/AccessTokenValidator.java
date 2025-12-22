package com.authsamples.api.plumbing.oauth;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.claims.CustomClaimNames;
import com.authsamples.api.plumbing.configuration.OAuthConfiguration;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.errors.ErrorUtils;
import com.authsamples.api.plumbing.logging.IdentityLogData;
import com.authsamples.api.plumbing.logging.LogEntry;
import com.authsamples.api.plumbing.logging.LogEntryImpl;
import tools.jackson.databind.ObjectMapper;

/*
 * A class to deal with OAuth JWT access token validation
 */
@Component
@Scope(value = CustomRequestScope.NAME)
public class AccessTokenValidator {

    private final OAuthConfiguration configuration;
    private final HttpsJwksVerificationKeyResolver jwksResolver;
    private final LogEntryImpl logEntry;

    public AccessTokenValidator(
            final OAuthConfiguration configuration,
            final HttpsJwksVerificationKeyResolver jwksResolver,
            final LogEntry logEntry) {

        this.configuration = configuration;
        this.jwksResolver = jwksResolver;
        this.logEntry = (LogEntryImpl) logEntry;
    }

    /*
     * Do the work of validating the access token and returning its claims
     */
    public JwtClaims execute(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("tokenValidator")) {

            var builder = new JwtConsumerBuilder()
                .setVerificationKeyResolver(this.jwksResolver)
                .setJwsAlgorithmConstraints(
                    AlgorithmConstraints.ConstraintType.PERMIT,
                    this.configuration.getAlgorithm()
                )
                .setExpectedIssuer(this.configuration.getIssuer());

            // Allow for AWS Cognito, which does not include an audience claim in access tokens
            if (StringUtils.hasLength(this.configuration.getAudience())) {
                builder.setExpectedAudience(this.configuration.getAudience());
            }

            // Validate the token and get its claims
            var jwtConsumer = builder.build();
            var claims = jwtConsumer.processToClaims(accessToken);

            // Add identity data to logs
            this.logEntry.setIdentityData(this.getIdentityData(claims));
            return claims;

        } catch (InvalidJwtException ex) {

            // Report failures
            throw ErrorUtils.fromAccessTokenValidationError(ex, this.configuration.getJwksEndpoint());
        }
    }

    /*
     * Collect identity data to add to logs
     */
    private IdentityLogData getIdentityData(final JwtClaims claims) {

        var data = new IdentityLogData();
        data.setUserId(ClaimsReader.getStringClaim(claims, "sub", false));
        data.setSessionId(ClaimsReader.getStringClaim(claims, "session_id", false));
        data.setClientId(ClaimsReader.getStringClaim(claims, "client_id", false));
        data.setScope(ClaimsReader.getStringClaim(claims, "scope", false));

        var mapper = new ObjectMapper();
        var claimsData = mapper.createObjectNode();
        claimsData.put("managerId", ClaimsReader.getStringClaim(claims, CustomClaimNames.ManagerId, false));
        claimsData.put("role", ClaimsReader.getStringClaim(claims, CustomClaimNames.Role, false));
        data.setClaims(claimsData);
        return data;
    }
}
