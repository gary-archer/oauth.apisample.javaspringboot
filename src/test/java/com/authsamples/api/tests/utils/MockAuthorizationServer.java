package com.authsamples.api.tests.utils;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.UUID;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.JoseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.authsamples.api.plumbing.claims.CustomClaimNames;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

/*
 * A mock authorization server implemented with an HTTP server and a JOSE library
 */
public final class MockAuthorizationServer {

    private HttpsServer httpsServer;
    private EllipticCurveJsonWebKey jwk;
    private String keyId;
    private String keysJson;

    public MockAuthorizationServer() {

        this.httpsServer = null;
        this.jwk = null;
        this.keyId = null;
        this.keysJson = null;

        // Reduce the jose4j library's log level
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.jose4j").setLevel(Level.WARN);
    }

    /*
     * Create resources at the start of the test run
     */
    public void start() throws JoseException {

        // Generate a JSON Web Key for our token issuing
        this.jwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        this.keyId = UUID.randomUUID().toString();
        this.jwk.setKeyId(this.keyId);
        this.jwk.setAlgorithm("ES256");

        // Publish the public keys at a JWKS URI
        var jsonWebKeySet = new JsonWebKeySet(this.jwk);
        this.keysJson = jsonWebKeySet.toJson();

        // Then start the HTTPS server
        this.startHttpsServer();
    }

    /*
     * Free resources at the end of the test run
     */
    public void stop() {
        httpsServer.stop(0);
    }

    /*
     * Issue an access token with a user and other values for testing
     * The access tokens for testing must match the structure of those issued by the real authorization server
     * https://bitbucket.org/b_c/jose4j/wiki/JWT%20Examples
     */
    public String issueAccessToken(MockTokenOptions options) throws JoseException {
        return this.issueAccessToken(options, this.jwk);
    }

    /*
     * An overload to allow a malicious key to be tested
     */
    public String issueAccessToken(
            MockTokenOptions options,
            EllipticCurveJsonWebKey jwk) throws JoseException {

        var claims = new JwtClaims();
        claims.setIssuer(options.getIssuer());
        claims.setAudience(options.getAudience());
        claims.setStringClaim("scope", options.getScope());
        claims.setStringClaim("delegation_id", options.getDelegationId());
        claims.setStringClaim("client_id", "TestClient");
        claims.setSubject(options.getSubject());
        claims.setStringClaim(CustomClaimNames.ManagerId, options.getManagerId());
        claims.setStringClaim(CustomClaimNames.Role, options.getRole());
        claims.setExpirationTimeMinutesInTheFuture(options.getExpiryMinutes());

        var jws = new JsonWebSignature();
        jws.setKeyIdHeaderValue(this.keyId);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);
        jws.setPayload(claims.toJson());
        jws.setKey(jwk.getPrivateKey());
        return jws.getCompactSerialization();
    }

    private void startHttpsServer() {

        try {

            var password = "Password1".toCharArray();
            var keyStore = KeyStore.getInstance("PKCS12");

            var path = Path.of("./certs/authsamples-dev.ssl.p12");
            try (var in = Files.newInputStream(path)) {
                keyStore.load(in, password);
            }

            var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);

            var sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), null, null);

            this.httpsServer = HttpsServer.create(new InetSocketAddress("login.authsamples-dev.com", 447), 0);

            this.httpsServer.setHttpsConfigurator(new com.sun.net.httpserver.HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    var sslParameters = sslContext.getDefaultSSLParameters();
                    params.setSSLParameters(sslParameters);
                }
            });

            this.httpsServer.createContext("/.well-known/jwks.json", exchange -> {

                byte[] response = this.keysJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            });

            this.httpsServer.start();

        } catch (Throwable ex) {
            throw new RuntimeException("Unable to start the mock authorization server", ex);
        }
    }
}
