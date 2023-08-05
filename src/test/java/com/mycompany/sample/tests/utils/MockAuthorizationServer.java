package com.mycompany.sample.tests.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import lombok.Getter;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * A mock authorization server implemented with wiremock and a JOSE library
 */
public final class MockAuthorizationServer {

    private final String adminBaseUrl;
    private RsaJsonWebKey jwk;

    @Getter
    private String keyId;

    public MockAuthorizationServer() {

        this.adminBaseUrl = "https://login.authsamples-dev.com:447/__admin/mappings";
        this.jwk = null;
        this.keyId = null;

        // Reduce the library's log level
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.jose4j").setLevel(Level.WARN);
    }

    /*
     * Create resources at the start of the test run
     */
    public void start() throws JoseException {

        // Generate a JSON Web Key for our token issuing
        this.jwk = RsaJwkGenerator.generateJwk(2048);
        this.keyId = UUID.randomUUID().toString();
        this.jwk.setKeyId(this.keyId);
        this.jwk.setAlgorithm("RS256");

        // Publish the public keys at a JWKS URI
        var jsonWebKeySet = new JsonWebKeySet(this.jwk);
        this.registerJsonWebWeys(jsonWebKeySet.toJson());
    }

    /*
     * Free resources at the start of the test run
     */
    public void stop() {
        this.unregisterJsonWebWeys();
    }

    /*
     * Issue an access token with the supplied user and other test options
     * https://bitbucket.org/b_c/jose4j/wiki/JWT%20Examples
     */
    public String issueAccessToken(final MockTokenOptions options) throws JoseException {
        return this.issueAccessToken(options, this.jwk);
    }

    /*
     * An overload to allow a malicious key to be tested
     */
    public String issueAccessToken(final MockTokenOptions options, final RsaJsonWebKey jwk) throws JoseException {

        var claims = new JwtClaims();
        claims.setIssuer(options.getIssuer());
        claims.setAudience(options.getAudience());
        claims.setStringClaim("scope", options.getScope());
        claims.setSubject(options.getSubject());
        claims.setStringClaim("manager_id", options.getManagerId());
        claims.setExpirationTimeMinutesInTheFuture(options.getExpiryMinutes());

        var jws = new JsonWebSignature();
        jws.setKeyIdHeaderValue(this.keyId);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setPayload(claims.toJson());
        jws.setKey(jwk.getPrivateKey());
        return jws.getCompactSerialization();
    }

    /*
     * Register our test JWKS values at the start of the test suite
     */
    private void registerJsonWebWeys(final String keysJson) {

        var mapper = new ObjectMapper();

        var data = mapper.createObjectNode();
        data.put("id", this.keyId);
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
    private void unregisterJsonWebWeys() {
        this.unregister(this.keyId);
    }

    /*
     * Add a stubbed response to Wiremock via its Admin API
     */
    private void register(final ObjectNode stubbedResponse) {

        HttpResponse<String> response;

        try {
            var request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(stubbedResponse.toString()))
                    .uri(new URI(this.adminBaseUrl))
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

            var url = String.format("%s/%s", this.adminBaseUrl, id);
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
