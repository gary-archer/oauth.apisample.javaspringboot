package com.mycompany.sample.plumbing.errors;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * The default implementation of a client error
 */
public final class ClientErrorImpl extends ClientError {

    private final HttpStatus statusCode;
    private final String errorCode;
    private JsonNode logContext;
    private String area;
    private int id;
    private String utcTime;

    /*
     * Construct from mandatory fields
     */
    public ClientErrorImpl(final HttpStatus statusCode, final String errorCode, final String message) {

        // Set mandatory fields
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;

        // Initialise optional fields
        this.logContext = null;
        this.area = null;
        this.id = 0;
        this.utcTime = null;
    }

    /*
     * As above but supports more detailed info
     */
    public void setLogContext(final JsonNode context) {
        this.logContext = context;
    }

    /*
     * When there is a 500 this is called by the exception handler
     */
    @Override
    public void setExceptionDetails(final String area, final int instanceId, final String utcTime) {
        this.area = area;
        this.id = instanceId;
        this.utcTime = utcTime;
    }

    @Override
    public HttpStatus getStatusCode() {
        return this.statusCode;
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    @Override
    public ObjectNode toResponseFormat() {

        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("code", this.errorCode);
        error.put("message", this.getMessage());

        if (this.id > 0 && this.area != null && this.utcTime != null) {
            error.put("area", this.area);
            error.put("id", this.id);
            error.put("utcTime", this.utcTime);
        }

        return error;
    }

    /*
     * Return the log format
     */
    @Override
    public ObjectNode toLogFormat() {

        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("statusCode", this.getStatusCode().value());
        error.set("clientError", this.toResponseFormat());

        if (this.logContext != null) {
            error.set("context", this.logContext);
        }

        return error;
    }
}
