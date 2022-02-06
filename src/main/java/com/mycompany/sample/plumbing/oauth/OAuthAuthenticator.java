package com.mycompany.sample.plumbing.oauth;

import java.net.URI;
import java.util.Collections;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
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

        } catch (HttpStatusCodeException ex) {

            // Report exceptions where we have a response body
            var errorData = ErrorResponseReader.tryReadJson(ex.getResponseBodyAsString());
            throw ErrorUtils.fromUserInfoError(
                    ex.getStatusCode(),
                    errorData,
                    this.configuration.getUserInfoEndpoint());

        } catch (Throwable ex) {

            // Report other exceptions
            throw ErrorUtils.fromUserInfoError(ex, this.configuration.getUserInfoEndpoint());
        }
    }
}
