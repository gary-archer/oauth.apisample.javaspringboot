package com.authsamples.api.plumbing.oauth;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.ErrorCodes;
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

            // See if this is a technical error downloading JSON web keys, which we report as a 500 error
            var jwksError = ErrorUtils.fromJwksDownloadError(ex, this.configuration.getJwksEndpoint());
            if (jwksError != null) {
                throw jwksError;
            }

            // For expired access tokens, add identity data to logs
            if (this.isAccessTokenExpiredError(ex)) {

                var claims = this.deserializeClaims(accessToken);
                if (claims != null) {
                    this.logEntry.setIdentityData(this.getIdentityData(claims));
                }
            }

            // Report 401s
            throw ErrorUtils.fromAccessTokenValidationError(ex, this.configuration.getJwksEndpoint());
        }
    }

    /*
     * Collect identity data to add to logs
     */
    private IdentityLogData getIdentityData(final JwtClaims claims) {

        var data = new IdentityLogData();
        data.setUserId(ClaimsReader.getStringClaim(claims, "sub", false));
        data.setSessionId(ClaimsReader.getStringClaim(claims, this.configuration.getSessionIdClaimName(), false));
        data.setClientId(ClaimsReader.getStringClaim(claims, "client_id", false));
        data.setScope(ClaimsReader.getStringClaim(claims, "scope", false));

        var mapper = new ObjectMapper();
        var claimsData = mapper.createObjectNode();
        claimsData.put("managerId", ClaimsReader.getStringClaim(claims, CustomClaimNames.ManagerId, false));
        claimsData.put("role", ClaimsReader.getStringClaim(claims, CustomClaimNames.Role, false));
        data.setClaims(claimsData);
        return data;
    }

    /*
     * The second condition is treated as expired, so that my expiry testing achieves the desired effect.
     * The expiry testing adds extra characters to JWTs to cause 401 errors and simulate expiry.
     */
    private boolean isAccessTokenExpiredError(final InvalidJwtException ex) {

        var errors = ex.getErrorDetails();
        for (var error: errors) {
            if (error.getErrorCode() == ErrorCodes.EXPIRED || error.getErrorCode() == ErrorCodes.SIGNATURE_INVALID) {
                return true;
            }
        }

        return false;
    }

    /*
     * Deserialize claims for logging purposes
     */
    private JwtClaims deserializeClaims(final String accessToken) {

        try {
            return new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build()
                    .process(accessToken)
                    .getJwtClaims();

        } catch (InvalidJwtException _) {
            return null;
        }
    }
}
