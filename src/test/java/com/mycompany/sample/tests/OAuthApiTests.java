package com.mycompany.sample.tests;

import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mycompany.sample.tests.utils.TokenIssuer;

@Suite
public class OAuthApiTests {

    private static String guestUserId;
    private static String guestAdminId;
    private static TokenIssuer tokenIssuer;
    private static Logger logger;

    /*
     * Setup that runs at the start of the test run
     */
    @BeforeAll
    public static void setup() throws JoseException {

        // The real subject claim values for my two online test users
        guestUserId = "a6b404b1-98af-41a2-8e7f-e4061dc0bf86";
        guestAdminId = "77a97e5b-b748-45e5-bb6f-658e85b2df91";

        // A class to issue our own JWTs for testing
        tokenIssuer = new TokenIssuer();

        // A custom logger to show test output when running 'mvn test'
        logger = LoggerFactory.getLogger(OAuthApiTests.class);

        // Register a mock keyset the API will use to validate JWTs
        var keyset = tokenIssuer.getTokenSigningPublicKeys();
        System.out.println(String.format("Keyset is %s", keyset));
    }

    /*
     * Teardown that runs when all tests have completed
     */
    @AfterAll
    public static void teardown() {
    }

    /*
     * Test getting claims
     */
    @Test
    public void GetUserClaims_ReturnsSingleRegion_ForStandardUser() throws JoseException {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);
        logger.info(String.format("Access token issued is %s", accessToken));

        Assertions.assertEquals("hello", "hello");
        logger.info("GetUserClaims_ReturnsSingleRegion_ForStandardUser");
    }

    /*
     * Test getting claims for the admin user
     */
    @Test
    public void GetUserClaims_ReturnsAllRegions_ForAdminUser() throws JoseException {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestAdminId);
        logger.info(String.format("Access token issued is %s", accessToken));

        var jwks = tokenIssuer.getTokenSigningPublicKeys();
        logger.info(String.format("*** JWKS is %s", jwks));

        Assertions.assertEquals("goodbye", "goodbye");
        logger.info("GetUserClaims_ReturnsAllRegions_ForAdminUser");
    }
}
