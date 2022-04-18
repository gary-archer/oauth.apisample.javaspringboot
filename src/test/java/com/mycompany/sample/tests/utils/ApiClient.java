package com.mycompany.sample.tests.utils;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

    public ApiResponse getUserInfoClaims(final ApiRequestOptions options) throws Throwable {

        options.setMethod("GET");
        options.setPath("/api/userinfo");

        return this.callApi(options);
    }

    public ApiResponse getCompanies(final ApiRequestOptions options) throws Throwable {

        options.setMethod("GET");
        options.setPath("/api/companies");;

        return this.callApi(options);
    }

    public ApiResponse getTransactions(final ApiRequestOptions options, final int companyId) throws Throwable {

        options.setMethod("GET");
        options.setPath(String.format("/api/companies/%d/transactions", companyId));

        return this.callApi(options);
    }

    private ApiResponse callApi(final ApiRequestOptions options) throws Throwable {

        var operationUrl = String.format("%s%s", this.baseUrl, options.getPath());
        var requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(operationUrl))
                .GET()
                .headers("Authorization", String.format("Bearer %s", options.getAccessToken()));

        if (options.getRehearseException()) {
            requestBuilder.headers("x-mycompany-test-exception", "SampleApi");
        }

        var request = requestBuilder.build();
        var client = HttpClient.newBuilder()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new ApiResponse(response);
    }
}
