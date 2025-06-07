package com.authsamples.api.tests;

import java.util.UUID;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.keys.EllipticCurves;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.platform.suite.api.Suite;
import com.authsamples.api.tests.utils.ApiClient;
import com.authsamples.api.tests.utils.ApiRequestOptions;
import com.authsamples.api.tests.utils.MockAuthorizationServer;
import com.authsamples.api.tests.utils.MockTokenOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Test the API in isolation, without any dependencies on real access tokens
 */
@Suite(failIfNoTests = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IntegrationTests {

    private static MockAuthorizationServer authorizationServer;
    private static ApiClient apiClient;

    /*
     * Setup that runs at the start of the test run
     */
    @BeforeAll
    public static void setupSuite() throws Throwable {

        // Uncomment to view HTTPS requests initiated from tests in an HTTP proxy
        // var url = new java.net.URL("http://127.0.0.1:8888");
        // System.setProperty("https.proxyHost", url.getHost());
        // System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

        // Run a mock authorization server
        authorizationServer = new MockAuthorizationServer();
        authorizationServer.start();

        // Create the API client
        String apiBaseUrl = "https://api.authsamples-dev.com:446";
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
     * Test that a request without an access token is rejected
     */
    @Test
    @Order(1)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns401_ForMissingJwt() throws Throwable {

        // Call the API and ensure a 401 response
        var apiOptions = new ApiRequestOptions("");
        var response = apiClient.getCompanies(apiOptions).join();
        Assertions.assertEquals(401, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the expected error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("invalid_token", errorCode.asText(), "Unexpected error code");
    }

    /*
     * Test that an expired access token is rejected
     */
    @Test
    @Order(2)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns401_ForExpiredJwt() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        jwtOptions.setExpiryMinutes(-30);
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

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
     * Test that an access token with an invalid issuer is rejected
     */
    @Test
    @Order(3)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns401_ForInvalidIssuer() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        jwtOptions.setIssuer("https://otherissuer.com");
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

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
     * Test that an access token with an invalid audience is rejected
     */
    @Test
    @Order(4)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns401_ForInvalidAudience() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        jwtOptions.setAudience("api.other.com");
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

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
     * Test that an access token with an invalid signature is rejected
     */
    @Test
    @Order(5)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns401_ForInvalidSignature() throws Throwable {

        // Get an access token for the end user of this test, issued with a malicious key
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        var maliciousJwk = EcJwkGenerator.generateJwk(EllipticCurves.P256);
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
     * Test that an access token with an invalid scope is rejected
     */
    @Test
    @Order(6)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_Returns403_ForInvalidScope() throws Throwable {

        // Get an access token for the end user of this test
        var jwtOptions = new MockTokenOptions();
        jwtOptions.useStandardUser();
        jwtOptions.setScope("openid profile");
        var accessToken = authorizationServer.issueAccessToken(jwtOptions);

        // Call the API and ensure a 401 response
        var apiOptions = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(apiOptions).join();
        Assertions.assertEquals(403, response.getStatusCode(), "Unexpected HTTP status");

        // Read the response and assert the expected error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("insufficient_scope", errorCode.asText(), "Unexpected error code");
    }

    /*
     * Test rehearsing a 500 error when there is an exception in the API
     */
    @Test
    @Order(7)
    @SuppressWarnings(value = "MethodName")
    public void CallApi_ReturnsSupportable500Error_ForErrorRehearsalRequest() throws Throwable {

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

    /*
     * Test getting business user attributes for the standard user
     */
    @Test
    @Order(8)
    @SuppressWarnings(value = "MethodName")
    public void GetUserInfo_ReturnsSingleRegion_ForStandardUser() throws Throwable {

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
     * Test getting business user attributes for the admin user
     */
    @Test
    @Order(9)
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
    @Order(10)
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
    @Order(11)
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
     * Test getting allowed transactions
     */
    @Test
    @Order(12)
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
    @Order(13)
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
}
