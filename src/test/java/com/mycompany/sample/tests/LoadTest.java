package com.mycompany.sample.tests;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
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
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.base.Strings;
import com.mycompany.sample.tests.utils.ApiClient;
import com.mycompany.sample.tests.utils.ApiResponse;
import com.mycompany.sample.tests.utils.TokenIssuer;

/*
 * A basic load test to ensure that the API behaves correctly when there are concurrent requests
 */
@Suite
public class LoadTest {

    private static TokenIssuer tokenIssuer;
    private static WireMockServer wiremock;
    private static ApiClient apiClient;
    private static String sessionId;
    private static String guestUserId;
    private static int totalCount;
    private static int errorCount;

    private static final String COLOR_BLUE   = "\u001B[34m";
    private static final String COLOR_GREEN  = "\u001B[32m";
    private static final String COLOR_RED  = "\u001B[31m";
    private static final String COLOR_YELLOW = "\u001B[33m";

    /*
     * Setup that runs at the start of the test run
     */
    @BeforeAll
    public static void setup() throws Throwable {

        // The real subject claim value for my online load test user
        guestUserId = "a6b404b1-98af-41a2-8e7f-e4061dc0bf86";

        // A class to issue our own JWTs for testing
        tokenIssuer = new TokenIssuer();

        // Start Wiremock to mock the Authorization Server and reduce log output
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        context.getLogger("org.eclipse.jetty").setLevel(Level.WARN);
        wiremock = new WireMockServer(80);
        wiremock.start();

        // The API will call the Authorization Server to get a JSON Web Key Set, so register a mock response
        var keyset = tokenIssuer.getTokenSigningPublicKeys();
        wiremock.stubFor(get(urlEqualTo("/.well-known/jwks.json")).willReturn(aResponse().withBody(keyset)));

        // The API will call the Authorization Server to get user info for the token, so register a mock response
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("given_name", "Guest");
        data.put("family_name", "User");
        data.put("email", "guestuser@mycompany.com");
        wiremock.stubFor(post(urlEqualTo("/oauth2/userInfo")).willReturn(aResponse().withBody(data.toString())));

        // Create the API client
        String apiBaseUrl = "https://api.authsamples-dev.com:445";
        sessionId = UUID.randomUUID().toString();
        apiClient = new ApiClient(apiBaseUrl, true);

        // Initialise counts
        totalCount = 0;
        errorCount = 0;
    }

    /*
     * Teardown that runs when the load test has completed
     */
    @AfterAll
    public static void teardown() {
        wiremock.stop();
    }

    /*
     * Run the load test
     */
    @Test
    public void run() throws Throwable {

        // Get some access tokens to send to the API
        var accessTokens = getAccessTokens();

        // Show a startup message
        var startTime = Instant.now();
        var startMessage = String.format(
                "%nLoad test session %s starting at %s%n",
                sessionId,
                startTime.toString());
        System.out.println(COLOR_BLUE + startMessage);

        var headings = new String[]{
                Strings.padEnd("OPERATION", 25, ' '),
                Strings.padEnd("CORRELATION-ID", 38, ' '),
                Strings.padEnd("START-TIME", 28, ' '),
                Strings.padEnd("MILLISECONDS-TAKEN", 21, ' '),
                Strings.padEnd("STATUS-CODE", 14, ' '),
                Strings.padEnd("ERROR-CODE", 24, ' '),
                Strings.padEnd("ERROR-ID", 12, ' ')
        };
        var header = String.join("", headings);
        System.out.println(COLOR_YELLOW + header);

        // Send the main body of requests
        sendLoadTestRequests(accessTokens);

        // Show a summary end message to finish
        var millisecondsTaken = Duration.between(startTime, Instant.now()).toMillis();
        var endMessage = String.format(
                "%nLoad test session %s completed in %s milliseconds: %d errors from %d requests%n",
                sessionId,
                millisecondsTaken,
                errorCount,
                totalCount);
        System.out.println(COLOR_BLUE + endMessage);
    }

    /*
     * Do some initial work to get multiple access tokens
     */
    private List<String> getAccessTokens() throws Exception {

        var list = new ArrayList<String>();
        for (int index = 0; index < 5; index++) {
            list.add(tokenIssuer.issueAccessToken(guestUserId));
        }
        return list;
    }

    /*
     * Run the main body of API requests, including some invalid requests that trigger errors
     */
    private void sendLoadTestRequests(final List<String> accessTokens) {

        // Next produce some requests that will run in parallel
        var requests = new ArrayList<Supplier<CompletableFuture<ApiResponse>>>();
        for (int index = 0; index < 100; index++) {
            System.out.println(COLOR_GREEN + String.format("Load Test item %d", index));
        }
    }
}
