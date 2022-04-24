package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.utilities.ErrorResponseReader;

/*
 * The entry point for calls to the Authorization Server
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
            return jwtConsumer.processToClaims(accessToken);

        } catch (InvalidJwtException ex) {

            // Report failures
            throw ErrorUtils.fromAccessTokenValidationError(ex, this.configuration.getJwksEndpoint());
        }
    }

    /*
     * Perform OAuth user info lookup via a plain HTTP request
     */
    public UserInfoClaims getUserInfo(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Construct the request
            var userInfoUrl = new URI(this.configuration.getUserInfoEndpoint());
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .uri(userInfoUrl)
                    .header("accept", "application/json")
                    .header("authorization", String.format("Bearer %s", accessToken))
                    .build();

            // Send it and get the response
            var client = HttpClient.newBuilder().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle errors returned from the server
            if (response.statusCode() != HttpStatus.OK.value()) {
                var errorData = ErrorResponseReader.tryReadJson(response.body());
                throw ErrorUtils.fromUserInfoError(
                        response.statusCode(),
                        errorData,
                        this.configuration.getUserInfoEndpoint());
            }

            // Parse the fields into an object
            var jsonText = response.body();
            var mapper = new ObjectMapper();
            var data = mapper.readValue(jsonText, ObjectNode.class);
            return ClaimsReader.userInfoClaims(data);

        } catch (Throwable ex) {

            // Report connectivity errors
            throw ErrorUtils.fromUserInfoError(ex, this.configuration.getUserInfoEndpoint());
        }
    }
}
