package com.mycompany.sample.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mycompany.sample.tests.utils.ApiClient;
import com.mycompany.sample.tests.utils.ApiRequestOptions;
import com.mycompany.sample.tests.utils.TokenIssuer;

@Suite
public class IntegrationTests {

    private static String guestUserId;
    private static String guestAdminId;
    private static TokenIssuer tokenIssuer;
    private static WireMockServer wiremock;
    private static ApiClient apiClient;

    /*
     * Setup that runs at the start of the test run
     */
    @BeforeAll
    public static void setup() throws Throwable {

        // The real subject claim values for my two online test users
        guestUserId = "a6b404b1-98af-41a2-8e7f-e4061dc0bf86";
        guestAdminId = "77a97e5b-b748-45e5-bb6f-658e85b2df91";

        // A class to issue our own JWTs for testing
        tokenIssuer = new TokenIssuer();

        // Reduce Wiremock output before starting it
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.eclipse.jetty").setLevel(Level.WARN);

        // Start Wiremock to mock the Authorization Server, which will listen on http://login.authsamples-dev.com:446
        var wiremockOptions = options()
                .port(446);
        wiremock = new WireMockServer(wiremockOptions);
        wiremock.start();

        // The API will call the Authorization Server to get a JSON Web Key Set, so register a mock response
        var keyset = tokenIssuer.getTokenSigningPublicKeys();
        wiremock.stubFor(get(urlEqualTo("/.well-known/jwks.json")).willReturn(aResponse().withBody(keyset)));

        // Create the API client
        String apiBaseUrl = "https://api.authsamples-dev.com:445";
        apiClient = new ApiClient(apiBaseUrl, false);
    }

    /*
     * Teardown that runs when all tests have completed
     */
    @AfterAll
    public static void teardown() {
        wiremock.stop();
    }

    /*
     * Test getting claims
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetUserClaims_ReturnsSingleRegion_ForStandardUser() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getUserInfoClaims(options).join();
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response regions and assert the count
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var regionsNode = (ArrayNode) body.get("regions");
        Assertions.assertEquals(1, regionsNode.size());
    }

    /*
     * Test getting claims
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetUserClaims_ReturnsAllRegions_ForAdminUser() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestAdminId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Admin");
        data.put("family_name", "User");
        data.put("email", "guestadmin@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getUserInfoClaims(options).join();
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response regions and assert the count
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var regionsNode = (ArrayNode) body.get("regions");
        Assertions.assertEquals(3, regionsNode.size());
    }

    /*
     * Test getting companies
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_ReturnsTwoItems_ForStandardUser() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(options).join();
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response and assert the count
        var body = mapper.readValue(response.getBody(), ArrayNode.class);
        Assertions.assertEquals(2, body.size());
    }

    /*
     * Test getting companies for the admin user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_ReturnsAllItems_ForAdminUser() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestAdminId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Admin");
        data.put("family_name", "User");
        data.put("email", "guestadmin@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(options).join();
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response and assert the count
        var body = mapper.readValue(response.getBody(), ArrayNode.class);
        Assertions.assertEquals(4, body.size());
    }

    /*
     * Test getting companies for the admin user
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetCompanies_Returns401_ForMaliciousJwt() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueMaliciousAccessToken(guestAdminId);

        // Call the API and ensure a 401 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanies(options).join();
        Assertions.assertEquals(401, response.getStatusCode());

        // Read the response and assert the expected error code
        var mapper = new ObjectMapper();
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("unauthorized", errorCode.asText());
    }

    /*
     * Test getting allowed transactions
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetTransactions_ReturnsAllowedItems_ForCompaniesMatchingTheRegionClaim() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestAdminId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanyTransactions(options, 2).join();
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response and assert the count
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var transactionsNode = (ArrayNode) body.get("transactions");
        Assertions.assertEquals(8, transactionsNode.size());
    }

    /*
     * Test getting unauthorized transactions
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetTransactions_ReturnsNotFoundForUser_ForCompaniesNotMatchingTheRegionClaim() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 404 response
        var options = new ApiRequestOptions(accessToken);
        var response = apiClient.getCompanyTransactions(options, 3).join();
        Assertions.assertEquals(404, response.getStatusCode());

        // Read the response and assert the error code
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("company_not_found", errorCode.asText());
    }

    /*
     * Test rehearsing a 500 error when there is an exception in the API
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void FailedApiCall_ReturnsSupportable500Error_ForErrorRehearsalRequest() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestUserId);

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 500 response
        var options = new ApiRequestOptions(accessToken);
        options.setRehearseException(true);
        var response = apiClient.getCompanyTransactions(options, 2).join();
        Assertions.assertEquals(500, response.getStatusCode());

        // Read the response and assert the error code
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("exception_simulation", errorCode.asText());
    }
}
