package com.mycompany.sample.tests.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Manage updates to Wiremock
 */
public final class WiremockAdmin {

    private final String baseUrl;
    private final String jsonWebKeysId;
    private final String userInfoId;

    public WiremockAdmin() {
        this.baseUrl = "https://login.authsamples-dev.com:447/__admin/mappings";
        this.jsonWebKeysId = UUID.randomUUID().toString();
        this.userInfoId = UUID.randomUUID().toString();
    }

    /*
     * Register our test JWKS values at the start of the test suite
     */
    public void registerJsonWebWeys(final String keysJson) {

        var mapper = new ObjectMapper();

        var data = mapper.createObjectNode();
        data.put("id", this.jsonWebKeysId);
        data.put("priority", 1);

        var request = mapper.createObjectNode();
        request.put("method", "GET");
        request.put("url", "/.well-known/jwks.json");
        data.set("request", request);

        var response = mapper.createObjectNode();
        response.put("status", 200);
        response.put("body", keysJson);
        data.set("response", response);

        this.register(data);
    }

    /*
     * Unregister our test JWKS values at the end of the test suite
     */
    public void unregisterJsonWebWeys() {
        this.unregister(this.jsonWebKeysId);
    }

    /*
     * Add a stubbed response to Wiremock via its Admin API
     */
    private void register(final ObjectNode stubbedResponse) {

        HttpResponse<String> response;

        try {
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(stubbedResponse.toString()))
                    .uri(new URI(this.baseUrl))
                    .header("content-type", "application/json")
                    .build();

            var client = HttpClient.newBuilder()
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

        if (response.statusCode() != 201) {
            var message = String.format("Failed to add Wiremock stub: status %d", response.statusCode());
            throw new RuntimeException(message);
        }
    }

    /*
     * Delete a stubbed response from Wiremock via its Admin API
     */
    private void unregister(final String id) {

        try {

            var url = String.format("%s/%s", this.baseUrl, id);
            var request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(new URI(url))
                    .build();

            var client = HttpClient.newBuilder()
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
