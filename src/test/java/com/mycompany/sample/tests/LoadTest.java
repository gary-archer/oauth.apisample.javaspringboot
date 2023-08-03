package com.mycompany.sample.tests;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.Suite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.mycompany.sample.tests.utils.ApiClient;
import com.mycompany.sample.tests.utils.ApiRequestOptions;
import com.mycompany.sample.tests.utils.ApiResponse;
import com.mycompany.sample.tests.utils.TokenIssuer;
import com.mycompany.sample.tests.utils.WiremockAdmin;

/*
 * A basic load test to ensure that the API behaves correctly when there are concurrent requests
 */
@Suite(failIfNoTests=false)
public class LoadTest {

    private static TokenIssuer tokenIssuer;
    private static WiremockAdmin wiremock;
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

        // Uncomment to view HTTPS requests initiated from tests in an HTTP proxy
        // var url = new java.net.URL("http://127.0.0.1:8888");
        // System.setProperty("https.proxyHost", url.getHost());
        // System.setProperty("https.proxyPort", String.valueOf(url.getPort()));

        // A class to issue our own JWTs for testing
        tokenIssuer = new TokenIssuer();
        wiremock = new WiremockAdmin();

        // The API will call the Authorization Server to get a JSON Web Key Set, so register a mock response
        var keyset = tokenIssuer.getTokenSigningPublicKeys();
        wiremock.registerJsonWebWeys(keyset);

        // Create the API client
        String apiBaseUrl = "https://apilocal.authsamples-dev.com:446";
        sessionId = UUID.randomUUID().toString();
        apiClient = new ApiClient(apiBaseUrl, "LoadTest", sessionId);

        // Initialise counts
        totalCount = 0;
        errorCount = 0;
    }

    /*
     * Teardown that runs when the load test has completed
     */
    @AfterAll
    public static void teardown() {
        wiremock.unregisterJsonWebWeys();
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
                Strings.padEnd("START-TIME", 30, ' '),
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

            // Create a 401 error on request 10, by making the access token act expired
            var accessToken = accessTokens.get(index % 5);
            if (index == 10) {
                accessToken += "x";
            }

            // Create some futures for various API endpoints
            if (index % 5 == 0) {

                requests.add(createUserInfoRequest(accessToken));

            } else if (index % 5 == 1) {

                requests.add(createTransactionsRequest(accessToken, 2));

            } else if (index % 5 == 2) {

                // On request 71 try to access unauthorized data for company 3, to create a 404 error
                var companyId = (index == 72) ? 3 : 2;
                requests.add(createTransactionsRequest(accessToken, companyId));

            } else {

                requests.add(createCompaniesRequest(accessToken));
            }
        }

        // Fire the API requests in batches
        executeApiRequests(requests);
    }

    /*
     * Prepare a get user info API request callback
     */
    private Supplier<CompletableFuture<ApiResponse>> createUserInfoRequest(final String accessToken) {

        var options = new ApiRequestOptions(accessToken);
        this.initializeApiRequest(options);
        return () -> apiClient.getUserInfoClaims(options);
    }

    /*
     * Prepare a get companies API request callback
     */
    private Supplier<CompletableFuture<ApiResponse>> createCompaniesRequest(final String accessToken) {

        var options = new ApiRequestOptions(accessToken);
        this.initializeApiRequest(options);
        return () -> apiClient.getCompanies(options);
    }

    /*
     * Prepare a get transactions API request callback
     */
    private Supplier<CompletableFuture<ApiResponse>> createTransactionsRequest(
            final String accessToken,
            final int companyId) {

        var options = new ApiRequestOptions(accessToken);
        this.initializeApiRequest(options);
        return () -> apiClient.getCompanyTransactions(options, companyId);
    }

    /*
     * Set any special logic before sending an API request
     */
    private void initializeApiRequest(final ApiRequestOptions options) {

        // On request 85 we'll simulate a 500 error via a custom header
        totalCount++;
        if (totalCount == 85) {
            options.setRehearseException(true);
        }
    }

    /*
     * Issue API requests in batches of 5, to avoid excessive queueing on a development computer
     * By default there is a limit of 5 concurrent outgoing requests to a single host
     */
    private void executeApiRequests(final List<Supplier<CompletableFuture<ApiResponse>>> requests) {

        // Set counters
        var total = requests.size();
        var batchSize = 5;
        var current = 0;

        // Process one batch at a time
        while (current < total) {

            // Get a batch of requests
            var requestBatch = requests.subList(current, Math.min(current + batchSize, total));

            // Start each API request in the batch concurrently, and return create a collection of futures
            var batchFutures = requestBatch.stream().map(this::executeApiRequest).toList();

            // Wait for all requests in the batch to complete
            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture<?>[0])).join();

            // Add to results
            current += batchSize;
        }
    }

    /*
     * Start execution and return a success future regardless of whether the API call succeeded
     */
    private CompletableFuture<ApiResponse> executeApiRequest(
            final Supplier<CompletableFuture<ApiResponse>> resultCallback) {

        Function<ApiResponse, CompletableFuture<ApiResponse>> callback = response -> {

            // Handle read errors
            if (response.getStatusCode() >= 200 && response.getStatusCode() <= 299) {

                // Report successful requests
                System.out.println(COLOR_GREEN + formatMetrics(response));

            } else {

                // Report failed requests
                System.out.println(COLOR_RED + formatMetrics(response));
                errorCount++;
            }

            return completedFuture(response);
        };

        return resultCallback.get().thenCompose(callback);
    }

    /*
     * Get metrics as a table row
     */
    private String formatMetrics(final ApiResponse response) {

        ObjectNode errorBody = null;
        var errorCode = "";
        var errorId   = "";

        if (response.getStatusCode() >= 400) {
            errorBody = this.deserializeErrorBody(response.getBody());
        }

        if (response.getStatusCode() >= 400 && errorBody != null) {
            errorCode = errorBody.get("code").asText();
        }

        if (response.getStatusCode() >= 500 && errorBody != null) {
            errorId = errorBody.get("id").asText();
        }

        var values = new String[]{
                Strings.padEnd(response.getMetrics().getOperation(), 25, ' '),
                Strings.padEnd(response.getMetrics().getCorrelationId(), 38, ' '),
                Strings.padEnd(response.getMetrics().getStartTime().toString(), 30, ' '),
                Strings.padEnd(String.valueOf(response.getMetrics().getMillisecondsTaken()), 21, ' '),
                Strings.padEnd(String.valueOf(response.getStatusCode()), 14, ' '),
                Strings.padEnd(errorCode, 24, ' '),
                Strings.padEnd(errorId, 12, ' ')
        };
        return String.join("", values);
    }

    /*
     * Avoid async calling code needing to use checked exceptions
     */
    private ObjectNode deserializeErrorBody(final String body) {

        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(body, ObjectNode.class);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
