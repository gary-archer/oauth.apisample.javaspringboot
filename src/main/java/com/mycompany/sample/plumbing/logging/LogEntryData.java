package com.mycompany.sample.plumbing.logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

/*
 * Each API request writes a structured log entry containing fields we will query by
 * It also writes JSON blobs whose fields are not designed to be queried
 */
@Data
public final class LogEntryData {

    // A unique generated client side id, which becomes the unique id in the aggregated logs database
    private String id;

    // The time when the API received the request
    private Instant utcTime;

    // The name of the API
    private String apiName;

    // The operation called
    private String operationName;

    // The host on which the request was processed
    private String hostName;

    // The HTTP verb
    private String requestVerb;

    // The request path
    private String requestPath;

    // The resource id(s) in the request URL path segments is often useful to query by
    private String resourceId;

    // The application that called the API
    private String clientApplicationName;

    // The subject claim from the OAuth 2.0 access token
    private String userOAuthId;

    // The status code returned
    private int statusCode;

    // The time taken in API code
    private long millisecondsTaken;

    // A time beyond which performance is considered 'slow'
    private int performanceThresholdMilliseconds;

    // The error code for requests that failed
    private String errorCode;

    // The specific error instance id, for 500 errors
    private int errorId;

    // The correlation id, used to link related API requests together
    private String correlationId;

    // A session id, to group related calls from a client together
    private String sessionId;

    // An object containing performance data, written when performance is slow
    private PerformanceBreakdownImpl performance;

    // An object containing error data, written for failed requests
    private ObjectNode errorData;

    // Can be populated in scenarios when extra text is useful
    private ArrayList<JsonNode> infoData;

    /*
     * Give fields default values
     */
    public LogEntryData() {

        // Queryable fields
        this.id = UUID.randomUUID().toString();
        this.utcTime = Instant.now();
        this.apiName = "";
        this.operationName = "";
        this.hostName = "";
        this.requestVerb = "";
        this.resourceId = "";
        this.requestPath = "";
        this.clientApplicationName = "";
        this.userOAuthId = "";
        this.statusCode = 0;
        this.millisecondsTaken = 0;
        this.performanceThresholdMilliseconds = 0;
        this.errorCode = "";
        this.errorId = 0;
        this.correlationId = "";
        this.sessionId = "";

        // Objects
        this.performance = new PerformanceBreakdownImpl("total");
        this.errorData = null;
        this.infoData = new ArrayList<>();
    }

    /*
     * Set fields at the end of a log entry
     */
    public void finalise() {
        this.millisecondsTaken = this.performance.getMillisecondsTaken();
    }

    /*
     * Produce the output format
     */
    public ObjectNode toLogFormat() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        // Add queryable informational fields
        this.outputString(x -> data.put("id", x), this.id);
        this.outputString(x -> data.put("utcTime", x), this.utcTime.toString());
        this.outputString(x -> data.put("apiName", x), this.apiName);
        this.outputString(x -> data.put("operationName", x), this.operationName);
        this.outputString(x -> data.put("hostName", x), this.hostName);
        this.outputString(x -> data.put("requestVerb", x), this.requestVerb);
        this.outputString(x -> data.put("resourceId", x), this.resourceId);
        this.outputString(x -> data.put("requestPath", x), this.requestPath);
        this.outputString(x -> data.put("clientApplicationName", x), this.clientApplicationName);
        this.outputString(x -> data.put("userOAuthId", x), this.userOAuthId);
        this.outputNumber(x -> data.put("statusCode", x), this.statusCode);
        this.outputString(x -> data.put("errorCode", x), this.errorCode);
        this.outputNumber(x -> data.put("errorId", x), this.errorId);
        this.outputNumberForce(x -> data.put("millisecondsTaken", x), this.millisecondsTaken);
        this.outputNumberForce(x -> data.put("millisecondsThreshold", x), this.performanceThresholdMilliseconds);
        this.outputString(x -> data.put("correlationId", x), this.correlationId);
        this.outputString(x -> data.put("sessionId", x), this.sessionId);

        // Add JSON text data
        this.outputPerformance(data);
        this.outputError(data);
        this.outputInfo(mapper, data);

        // Return the JSON object
        return data;
    }

    /*
     * Indicate whether an error entry
     */
    public boolean isError() {
        return this.errorData != null;
    }

    /*
     * Add a string to the output unless empty
     */
    private void outputString(final Consumer<String> setter, final String value) {

        if (value != null && value.length() > 0) {
            setter.accept(value);
        }
    }

    /*
     * Add an integer to the output unless not set or zero
     */
    private void outputNumber(final Consumer<Long> setter, final long value) {

        if (value > 0) {
            setter.accept(value);
        }
    }

    /*
     * Add an integer to the output including zeros
     */
    private void outputNumberForce(final Consumer<Long> setter, final long value) {
        setter.accept(value);
    }

    /*
     * Add the performance breakdown if the threshold has been exceeded
     */
    private void outputPerformance(final ObjectNode root) {

        if (this.performance.getMillisecondsTaken() >= this.performanceThresholdMilliseconds || errorId > 0) {
            root.set("performance", this.performance.getData());
        }
    }

    /*
     * Add error details if applicable
     */
    private void outputError(final ObjectNode root) {

        if (this.errorData != null) {
            root.set("errorData", this.errorData);
        }
    }

    /*
     * Add free text info details if used
     */
    private void outputInfo(final ObjectMapper mapper, final ObjectNode root) {

        if (this.infoData.size() > 0) {
            var infoNode = mapper.createArrayNode();
            for (var info : this.infoData) {
                infoNode.add(info);
            }

            root.set("infoData", infoNode);
        }
    }
}
