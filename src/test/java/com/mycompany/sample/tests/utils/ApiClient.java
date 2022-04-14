package com.mycompany.sample.tests.utils;

import com.mycompany.sample.plumbing.claims.UserInfoClaims;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
 * A utility class to call the API in a parameterized manner
 */
public final class ApiClient {

    private final String baseUrl;

    public ApiClient(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UserInfoClaims getUserInfoClaims(final String accessToken) {
        return new UserInfoClaims("Guest", "User", "guestuser@mycompany.com");
    }

    private void callApi(final String path, final String accessToken) throws Throwable {

        var operationUrl = String.format("%s/%s", this.baseUrl, path);
        var request = HttpRequest.newBuilder()
                .uri(new URI(operationUrl))
                .GET()
                .headers("Authorization", String.format("Bearer %s"), accessToken)
                .build();

        var client = HttpClient.newBuilder()
                // .proxy(ProxySelector.getDefault())
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        // TODO: return a JsonNode generic response
    }
}
