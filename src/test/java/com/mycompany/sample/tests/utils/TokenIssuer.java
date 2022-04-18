package com.mycompany.sample.tests.utils;

import java.util.UUID;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/*
 * A token issuer for testing
 */
public final class TokenIssuer {

    private final RsaJsonWebKey jwk;
    private final String keyId;

    /*
     * Do the key setup during construction
     */
    public TokenIssuer() throws JoseException {

        // Reduce the library's log level
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.jose4j").setLevel(Level.WARN);

        // Generate a JSON Web Key for our token issuing
        this.jwk = RsaJwkGenerator.generateJwk(2048);
        this.keyId = UUID.randomUUID().toString();
        this.jwk.setKeyId(this.keyId);
        this.jwk.setAlgorithm("RS256");
    }

    /*
     * Issue an access token with the supplied subject claim
     * https://bitbucket.org/b_c/jose4j/wiki/JWT%20Examples
     */
    public String issueAccessToken(final String sub) throws JoseException {

     var claims = new JwtClaims();
        claims.setSubject(sub);
        claims.setIssuer("testissuer.com");
        claims.setAudience("api.mycompany.com");
        claims.setStringClaim("scope", "openid profile email https://api.authsamples.com/api/transactions_read");
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setNotBeforeMinutesInThePast(1);

        var jws = new JsonWebSignature();
        jws.setKeyIdHeaderValue(this.keyId);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setPayload(claims.toJson());
        jws.setKey(this.jwk.getPrivateKey());
        return jws.getCompactSerialization();
    }

    /*
     * Get the token signing public keys as a JSON Web Keyset
     */
    public String getTokenSigningPublicKeys() {

        var jsonWebKeySet = new JsonWebKeySet(this.jwk);
        return jsonWebKeySet.toJson();
    }
}
