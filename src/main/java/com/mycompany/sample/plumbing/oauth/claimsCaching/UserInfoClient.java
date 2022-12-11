package com.mycompany.sample.plumbing.oauth.claimsCaching;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.plumbing.claims.ClaimsReader;
import com.mycompany.sample.plumbing.claims.UserInfoClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorResponseReader;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;

/*
 * The entry point for calls to the Authorization Server
 */
@Component
@Scope(value = CustomRequestScope.NAME)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class UserInfoClient {

    private final OAuthConfiguration configuration;
    private final LogEntry logEntry;

    public UserInfoClient(final OAuthConfiguration configuration, final LogEntry logEntry) {

        this.configuration = configuration;
        this.logEntry = logEntry;
    }

    /*
     * Perform OAuth user info lookup via a plain HTTP request
     */
    public UserInfoClaims getUserInfo(final String accessToken) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("userInfoLookup")) {

            // Construct the request
            var userInfoUrl = new URI(this.configuration.getClaimsCache().getUserInfoEndpoint());
            var request = HttpRequest.newBuilder()
                    .GET()
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
                        this.configuration.getClaimsCache().getUserInfoEndpoint());
            }

            // Parse the fields into an object
            var jsonText = response.body();
            var mapper = new ObjectMapper();
            var data = mapper.readValue(jsonText, ObjectNode.class);
            return ClaimsReader.userInfoClaims(data);

        } catch (Throwable ex) {

            // Report connectivity errors
            throw ErrorUtils.fromUserInfoError(ex, this.configuration.getClaimsCache().getUserInfoEndpoint());
        }
    }
}
