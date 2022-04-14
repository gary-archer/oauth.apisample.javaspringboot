package com.mycompany.sample.tests;

import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mycompany.sample.tests.utils.ApiClient;
import com.mycompany.sample.tests.utils.TokenIssuer;

@Suite
public class OAuthApiTests {

    private static String guestUserId;
    private static String guestAdminId;
    private static TokenIssuer tokenIssuer;
    private static ApiClient apiClient;
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

        // Create the API client
        String apiBaseUrl = "https://api.authsamples-dev.com:445";
        apiClient = new ApiClient(apiBaseUrl);
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
    @SuppressWarnings(value = "MethodName")
    public void GetUserClaims_ReturnsSingleRegion_ForStandardUser() throws JoseException {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);

        // Call the API
        var response = apiClient.getUserInfoClaims(accessToken);

        Assertions.assertEquals(response.getGivenName(), "Guest");
        logger.info("GetUserClaims_ReturnsSingleRegion_ForStandardUser");
    }
}
