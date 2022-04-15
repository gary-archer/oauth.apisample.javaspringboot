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
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mycompany.sample.tests.utils.ApiClient;
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

        // Start Wiremock to mock the Authorization Server and reduce log output
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.eclipse.jetty").setLevel(Level.WARN);
        wiremock = new WireMockServer(80);
        wiremock.start();

        // Register a mock keyset the API will use to validate JWTs
        var keyset = tokenIssuer.getTokenSigningPublicKeys();
        wiremock.stubFor(get(urlEqualTo("/.well-known/jwks.json")).willReturn(aResponse().withBody(keyset)));

        // Create the API client
        String apiBaseUrl = "https://api.authsamples-dev.com:445";
        apiClient = new ApiClient(apiBaseUrl, true);
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

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getUserInfoClaims(accessToken);
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

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Admin");
        data.put("family_name", "User");
        data.put("email", "guestadmin@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getUserInfoClaims(accessToken);
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

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getCompanies(accessToken);
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

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Admin");
        data.put("family_name", "User");
        data.put("email", "guestadmin@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getCompanies(accessToken);
        Assertions.assertEquals(200, response.getStatusCode());

        // Read the response and assert the count
        var body = mapper.readValue(response.getBody(), ArrayNode.class);
        Assertions.assertEquals(4, body.size());
    }

    /*
     * Test getting allowed transactions
     */
    @Test
    @SuppressWarnings(value = "MethodName")
    public void GetTransactions_ReturnsAllowedItems_ForCompaniesMatchingTheRegionClaim() throws Throwable {

        // Get an access token for the end user of this test
        var accessToken = tokenIssuer.issueAccessToken(guestAdminId);

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getTransactions(accessToken, 2);
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

        // Register the Authorization Server response to a user info request from the API
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Call the API and ensure a 200 response
        var response = apiClient.getTransactions(accessToken, 3);
        Assertions.assertEquals(404, response.getStatusCode());

        // Read the response and assert the count
        var body = mapper.readValue(response.getBody(), ObjectNode.class);
        var errorCode = body.get("code");
        Assertions.assertEquals("company_not_found", errorCode.asText());
    }
}
