package com.mycompany.sample.tests.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/*
 * A utility class to call the API in a parameterized manner
 */
public final class ApiClient {

    private final String baseUrl;

    public ApiClient(final String baseUrl, final Boolean useProxy) throws Throwable {

        this.baseUrl = baseUrl;
        if (useProxy) {
            var url = new URL("http://127.0.0.1:8888");
            System.setProperty("http.proxyHost", url.getHost());
            System.setProperty("http.proxyPort", String.valueOf(url.getPort()));
        }
    }

    public CompletableFuture<ApiResponse> getUserInfoClaims(final ApiRequestOptions options) {

        options.setMethod("GET");
        options.setPath("/api/userinfo");

        return this.callApi(options);
    }

    public CompletableFuture<ApiResponse> getCompanies(final ApiRequestOptions options) {

        options.setMethod("GET");
        options.setPath("/api/companies");

        return this.callApi(options);
    }

    public CompletableFuture<ApiResponse> getTransactions(final ApiRequestOptions options, final int companyId) {

        options.setMethod("GET");
        options.setPath(String.format("/api/companies/%d/transactions", companyId));

        return this.callApi(options);
    }

    /*
     * Parameterized code to do the async work of calling the API
     */
    private CompletableFuture<ApiResponse> callApi(final ApiRequestOptions options) {

        // Prepare the request
        var operationUrl = String.format("%s%s", this.baseUrl, options.getPath());
        var requestBuilder = HttpRequest.newBuilder()
                .uri(this.stringToUri(operationUrl))
                .GET()
                .headers("Authorization", String.format("Bearer %s", options.getAccessToken()));

        if (options.getRehearseException()) {
            requestBuilder.headers("x-mycompany-test-exception", "SampleApi");
        }

        var request = requestBuilder.build();
        var client = HttpClient.newBuilder()
                .build();

        // Handle the response
        BiFunction<HttpResponse<String>, Throwable, ApiResponse> callback = (response, ex) -> {

            // Handle read errors
            if (ex != null) {
                throw new RuntimeException(ex);
            }

            return new ApiResponse(response);
        };

        // Send the request
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).handle(callback);
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
