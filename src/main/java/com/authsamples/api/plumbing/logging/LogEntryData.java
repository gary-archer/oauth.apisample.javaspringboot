package com.authsamples.api.plumbing.logging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Data;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/*
 * Log data collected during the lifetime of an API request
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

    // The HTTP vmethod
    private String method;

    // The request path
    private String path;

    // The resource id(s) in the request URL path segments is often useful to query by
    private String resourceId;

    // The application that called the API
    private String clientName;

    // The subject claim from the OAuth 2.0 access token
    private String userId;

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

    // The OAuth scopes from the access token
    private List<String> scope;

    // The OAuth claims from the access token
    private ObjectNode claims;

    /*
     * Give fields default values
     */
    public LogEntryData() {

        this.id = UUID.randomUUID().toString();
        this.utcTime = Instant.now();
        this.apiName = "";
        this.operationName = "";
        this.hostName = "";
        this.method = "";
        this.path = "";
        this.resourceId = "";
        this.clientName = "";
        this.userId = "";
        this.statusCode = 0;
        this.millisecondsTaken = 0;
        this.performanceThresholdMilliseconds = 0;
        this.errorCode = "";
        this.errorId = 0;
        this.correlationId = "";
        this.sessionId = "";
        this.performance = new PerformanceBreakdownImpl("total");
        this.errorData = null;
        this.infoData = new ArrayList<>();
        this.scope = new ArrayList<>();
        this.claims = null;
    }

    /*
     * Set fields at the end of a log entry
     */
    public void finalise() {
        this.millisecondsTaken = this.performance.getMillisecondsTaken();
    }

    /*
     * Output technical support details for troubleshooting but without sensitive data
     */
    public ObjectNode toRequestLog() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        data.put("type", "request");
        this.outputString(x -> data.put("id", x), this.id);
        this.outputString(x -> data.put("utcTime", x), this.utcTime.toString());
        this.outputString(x -> data.put("apiName", x), this.apiName);
        this.outputString(x -> data.put("operationName", x), this.operationName);
        this.outputString(x -> data.put("hostName", x), this.hostName);
        this.outputString(x -> data.put("method", x), this.method);
        this.outputString(x -> data.put("path", x), this.path);
        this.outputString(x -> data.put("resourceId", x), this.resourceId);
        this.outputString(x -> data.put("clientName", x), this.clientName);
        this.outputString(x -> data.put("userId", x), this.userId);
        this.outputNumber(x -> data.put("statusCode", x), this.statusCode);
        this.outputString(x -> data.put("errorCode", x), this.errorCode);
        this.outputNumber(x -> data.put("errorId", x), this.errorId);
        this.outputNumberForce(x -> data.put("millisecondsTaken", x), this.millisecondsTaken);
        this.outputString(x -> data.put("correlationId", x), this.correlationId);
        this.outputString(x -> data.put("sessionId", x), this.sessionId);

        this.outputPerformance(data);
        this.outputError(data);
        this.outputInfo(mapper, data);
        return data;
    }

    /*
     * Output audit logs for security visibility but without troubleshooting data
     */
    public ObjectNode toAuditLog() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        data.put("type", "audit");
        this.outputString(x -> data.put("id", x), this.id);
        this.outputString(x -> data.put("utcTime", x), this.utcTime.toString());
        this.outputString(x -> data.put("apiName", x), this.apiName);
        this.outputString(x -> data.put("operationName", x), this.operationName);
        this.outputString(x -> data.put("hostName", x), this.hostName);
        this.outputString(x -> data.put("method", x), this.method);
        this.outputString(x -> data.put("path", x), this.path);
        this.outputString(x -> data.put("resourceId", x), this.resourceId);
        this.outputString(x -> data.put("clientName", x), this.clientName);
        this.outputString(x -> data.put("userId", x), this.userId);
        this.outputNumber(x -> data.put("statusCode", x), this.statusCode);
        this.outputString(x -> data.put("errorCode", x), this.errorCode);
        this.outputString(x -> data.put("correlationId", x), this.correlationId);
        this.outputString(x -> data.put("sessionId", x), this.sessionId);

        var isAuthenticated = !this.userId.isEmpty();
        data.put("isAuthenticated", isAuthenticated);
        data.put("isAuthorized", isAuthenticated && (this.statusCode >= 200 && this.statusCode <= 299));

        if (!this.scope.isEmpty()) {
            var scopeNode = mapper.createArrayNode();
            for (var scopeItem: this.scope) {
                scopeNode.add(scopeItem);
            }
            data.set("scope", scopeNode);
        }

        if (this.claims != null) {
            data.set("claims", claims);
        }

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

        if (value != null && !value.isEmpty()) {
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

        if (this.performance.getMillisecondsTaken() >= this.performanceThresholdMilliseconds) {
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

        if (!this.infoData.isEmpty()) {
            var infoNode = mapper.createArrayNode();
            for (var info : this.infoData) {
                infoNode.add(info);
            }

            root.set("infoData", infoNode);
        }
    }
}
