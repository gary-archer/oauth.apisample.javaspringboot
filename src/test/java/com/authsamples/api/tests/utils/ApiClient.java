package com.authsamples.api.tests.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/*
 * A utility class to call the API in a parameterized manner
 */
public final class ApiClient {

    private final String baseUrl;
    private final String clientName;
    private final String sessionId;

    public ApiClient(
            final String baseUrl,
            final String clientName,
            final String sessionId) {

        this.baseUrl = baseUrl;
        this.clientName = clientName;
        this.sessionId = sessionId;
    }

    public CompletableFuture<ApiResponse> getUserInfoClaims(final ApiRequestOptions options) {

        options.setMethod("GET");
        options.setPath("/investments/userinfo");

        var metrics = new ApiResponseMetrics("getUserInfoClaims");
        return this.callApi(options, metrics);
    }

    public CompletableFuture<ApiResponse> getCompanies(final ApiRequestOptions options) {

        options.setMethod("GET");
        options.setPath("/investments/companies");

        var metrics = new ApiResponseMetrics("getCompanies");
        return this.callApi(options, metrics);
    }

    public CompletableFuture<ApiResponse> getCompanyTransactions(final ApiRequestOptions options, final int companyId) {

        options.setMethod("GET");
        options.setPath(String.format("/investments/companies/%d/transactions", companyId));

        var metrics = new ApiResponseMetrics("getCompanyTransactions");
        return this.callApi(options, metrics);
    }

    /*
     * Parameterized code to do the async work of calling the API
     */
    private CompletableFuture<ApiResponse> callApi(final ApiRequestOptions options, final ApiResponseMetrics metrics) {

        // Initialize metrics
        var correlationId = UUID.randomUUID().toString();
        metrics.setStartTime(Instant.now());
        metrics.setCorrelationId(correlationId);

        // Prepare the request
        var operationUrl = String.format("%s%s", this.baseUrl, options.getPath());
        var requestBuilder = HttpRequest.newBuilder()
                .method(options.getMethod(), HttpRequest.BodyPublishers.noBody())
                .uri(this.stringToUri(operationUrl))
                .headers("Authorization", String.format("Bearer %s", options.getAccessToken()))
                .headers("x-authsamples-api-client", this.clientName)
                .headers("x-authsamples-session-id", this.sessionId)
                .headers("x-authsamples-correlation-id", correlationId);

        if (options.getRehearseException()) {
            requestBuilder.headers("x-authsamples-test-exception", "FinalApi");
        }

        var request = requestBuilder.build();
        var client = HttpClient.newBuilder()
                .build();

        // Send the request
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).handle((response, ex) -> {

            // Record the time taken in all cases
            metrics.setMillisecondsTaken(Duration.between(metrics.getStartTime(), Instant.now()).toMillis());

            // Connection errors will abort the test
            if (ex != null) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }

            // Return both success and error responses received from the API
            return new ApiResponse(response, metrics);
        });
    }

    /*
     * Avoid async calling code needing to use checked exceptions
     */
    private URI stringToUri(final String uri) {

        try {
            return new URI(uri);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
