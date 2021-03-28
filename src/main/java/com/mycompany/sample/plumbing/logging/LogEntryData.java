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
    private String _id;

    // The time when the API received the request
    private Instant _utcTime;

    // The name of the API
    private String _apiName;

    // The operation called
    private String _operationName;

    // The host on which the request was processed
    private String _hostName;

    // The HTTP verb
    private String _requestVerb;

    // The request path
    private String _requestPath;

    // The resource id(s) in the request URL path segments is often useful to query by
    private String _resourceId;

    // The application that called the API
    private String _clientApplicationName;

    // The subject claim from the OAuth 2.0 access token
    private String _userOAuthId;

    // The status code returned
    private int _statusCode;

    // The time taken in API code
    private long _millisecondsTaken;

    // A time beyond which performance is considered 'slow'
    private int _performanceThresholdMilliseconds;

    // The error code for requests that failed
    private String _errorCode;

    // The specific error instance id, for 500 errors
    private int _errorId;

    // The correlation id, used to link related API requests together
    private String _correlationId;

    // A session id, to group related calls from a client together
    private String _sessionId;

    // An object containing performance data, written when performance is slow
    private PerformanceBreakdownImpl _performance;

    // An object containing error data, written for failed requests
    private ObjectNode _errorData;

    // Can be populated in scenarios when extra text is useful
    private ArrayList<JsonNode> _infoData;

    /*
     * Give fields default values
     */
    public LogEntryData() {

        // Queryable fields
        this._id = UUID.randomUUID().toString();
        this._utcTime = Instant.now();
        this._apiName = "";
        this._operationName = "";
        this._hostName = "";
        this._requestVerb = "";
        this._resourceId = "";
        this._requestPath = "";
        this._clientApplicationName = "";
        this._userOAuthId = "";
        this._statusCode = 0;
        this._millisecondsTaken = 0;
        this._performanceThresholdMilliseconds = 0;
        this._errorCode = "";
        this._errorId = 0;
        this._correlationId = "";
        this._sessionId = "";

        // Objects
        this._performance = new PerformanceBreakdownImpl("total");
        this._errorData = null;
        this._infoData = new ArrayList<>();
    }

    /*
     * Set fields at the end of a log entry
     */
    public void finalise() {
        this._millisecondsTaken = this._performance.getMillisecondsTaken();
    }

    /*
     * Produce the output format
     */
    public ObjectNode toLogFormat() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        // Add queryable informational fields
        this.outputString(x -> data.put("id", x), this._id);
        this.outputString(x -> data.put("utcTime", x), this._utcTime.toString());
        this.outputString(x -> data.put("apiName", x), this._apiName);
        this.outputString(x -> data.put("operationName", x), this._operationName);
        this.outputString(x -> data.put("hostName", x), this._hostName);
        this.outputString(x -> data.put("requestVerb", x), this._requestVerb);
        this.outputString(x -> data.put("resourceId", x), this._resourceId);
        this.outputString(x -> data.put("requestPath", x), this._requestPath);
        this.outputString(x -> data.put("clientApplicationName", x), this._clientApplicationName);
        this.outputString(x -> data.put("userOAuthId", x), this._userOAuthId);
        this.outputNumber(x -> data.put("statusCode", x), this._statusCode);
        this.outputString(x -> data.put("errorCode", x), this._errorCode);
        this.outputNumber(x -> data.put("errorId", x), this._errorId);
        this.outputNumberForce(x -> data.put("millisecondsTaken", x), this._millisecondsTaken);
        this.outputNumberForce(x -> data.put("millisecondsThreshold", x), this._performanceThresholdMilliseconds);
        this.outputString(x -> data.put("correlationId", x), this._correlationId);
        this.outputString(x -> data.put("sessionId", x), this._sessionId);

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
        return this._errorData != null;
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

        if (this._performance.getMillisecondsTaken() >= this._performanceThresholdMilliseconds || _errorId > 0) {
            root.set("performance", this._performance.getData());
        }
    }

    /*
     * Add error details if applicable
     */
    private void outputError(final ObjectNode root) {

        if (this._errorData != null) {
            root.set("errorData", this._errorData);
        }
    }

    /*
     * Add free text info details if used
     */
    private void outputInfo(final ObjectMapper mapper, final ObjectNode root) {

        if (this._infoData.size() > 0) {
            var infoNode = mapper.createArrayNode();
            for (var info : this._infoData) {
                infoNode.add(info);
            }

            root.set("infoData", infoNode);
        }
    }
}
