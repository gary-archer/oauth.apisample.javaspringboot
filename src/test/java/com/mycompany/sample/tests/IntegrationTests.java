package com.mycompany.sample.tests;

import java.util.UUID;

import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.*;
import org.junit.platform.suite.api.Suite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.tests.utils.ApiClient;
import com.mycompany.sample.tests.utils.ApiRequestOptions;
import com.mycompany.sample.tests.utils.MockAuthorizationServer;
import com.mycompany.sample.tests.utils.MockTokenOptions;

@Suite(failIfNoTests = false)
public class IntegrationTests {

    private static MockAuthorizationServer authorizationServer;
    private static ApiClient apiClient;

    /*
     * Setup that runs at the start of the test run
     */
    @BeforeAll
    public static void setupSuite() throws Throwable {

        // Uncomment to view HTTPS requests initiated from tests in an HTTP proxy
        // var url = new URL("http://127.0.0.1:8888");
        // System.setProperty("https.proxyHost", url.getHost());
        // System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

        // Create a mock authorization server
        authorizationServer = new MockAuthorizationServer();
        authorizationServer.start();

        // Create the API client
        String apiBaseUrl = "https://apilocal.authsamples-dev.com:446";
        var sessionId = UUID.randomUUID().toString();
        apiClient = new ApiClient(apiBaseUrl, "IntegrationTests", sessionId);
    }

    /*
     * Teardown that runs when all tests have completed
     */
    @AfterAll
    public static void teardownSuite() {
        authorizationServer.stop();
    }

    /*
     * Test getting claims as the standard user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetUserClaims_ReturnsSingleRegion_ForStandardUser() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 200 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getUserInfoClaims(apiOptions).join();
        Assertions.assertEquals(200, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response regions and assert the count
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var regionsNode = (ArrayNode) body.get("regions");
        Assertions.assertEquals(1, regionsNode.size(), "Unexpected regions claim");
    }

    /*
     * Test getting claims as the admin user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetUserClaims_ReturnsAllRegions_ForAdminUser() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useAdminUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 200 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getUserInfoClaims(apiOptions).join();
        Assertions.assertEquals(200, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response regions and assert the count
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var regionsNode = (ArrayNode) body.get("regions");
        Assertions.assertEquals(3, regionsNode.size(), "Unexpected regions claim");
    }

    /*
     * Test getting companies as the standard user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_ReturnsTwoItems_ForStandardUser() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 200 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(apiOptions).join();
        Assertions.assertEquals(200, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the count
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ArrayNode.class);
        Assertions.assertEquals(2, body.size(), "Unexpected companies list");
    }

    /*
     * Test getting companies for the admin user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_ReturnsAllItems_ForAdminUser() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useAdminUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 200 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(apiOptions).join();
        Assertions.assertEquals(200, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the count
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ArrayNode.class);
        Assertions.assertEquals(4, body.size(), "Unexpected companies list");
    }

    /*
     * Test getting companies with a malicious JWT access token
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_Returns401_ForMaliciousJwt() throws Throwable {

        var maliciousJwk = RsaJwkGenerator.generateJwk(2048);
        maliciousJwk.setKeyId(authorizationServer.getKeyId());
        maliciousJwk.setAlgorithm("RS256");

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions, maliciousJwk);

        // Call the API and ensure a 401 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(apiOptions).join();
        Assertions.assertEquals(401, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the expected error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("invalid_token", errorCode.asText(), "Unexpected error code");
    }

    /*
     * Test getting allowed transactions
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetTransactions_ReturnsAllowedItems_ForCompaniesMatchingTheRegionClaim() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 200 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanyTransactions(apiOptions, 2).join();
        Assertions.assertEquals(200, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the count
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var transactionsNode = (ArrayNode) body.get("transactions");
        Assertions.assertEquals(8, transactionsNode.size(), "Unexpected transactions");
    }

    /*
     * Test getting unauthorized transactions
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetTransactions_ReturnsNotFoundForUser_ForCompaniesNotMatchingTheRegionClaim() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 404 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanyTransactions(apiOptions, 3).join();
        Assertions.assertEquals(404, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("company_not_found", errorCode.asText(), "Unexpected error code");
    }

    /*
     * Test rehearsing a 500 error when there is an exception in the API
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void FailedApiCall_ReturnsSupportable500Error_ForErrorRehearsalRequest() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 500 response
        var apiOptions = new ApiRequestOptions(accessToken);
        apiOptions.setRehearseException(true);
        var response = apiClient.getCompanyTransactions(apiOptions, 2).join();
        Assertions.assertEquals(500, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("exception_simulation", errorCode.asText(), "Unexpected error code");
    }
}
