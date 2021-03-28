package com.mycompany.sample.plumbing.errors;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * The default implementation of a client error
 */
public final class ClientErrorImpl extends ClientError {

    private final HttpStatus _statusCode;
    private final String _errorCode;
    private JsonNode _logContext;
    private String _area;
    private int _id;
    private String _utcTime;

    /*
     * Construct from mandatory fields
     */
    public ClientErrorImpl(final HttpStatus statusCode, final String errorCode, final String message) {

        // Set mandatory fields
        super(message);
        this._statusCode = statusCode;
        this._errorCode = errorCode;

        // Initialise optional fields
        this._logContext = null;
        this._area = null;
        this._id = 0;
        this._utcTime = null;
    }

    /*
     * As above but supports more detailed info
     */
    public void setLogContext(final JsonNode context) {
        this._logContext = context;
    }

    /*
     * When there is a 500 this is called by the exception handler
     */
    @Override
    public void setExceptionDetails(final String area, final int instanceId, final String utcTime) {
        this._area = area;
        this._id = instanceId;
        this._utcTime = utcTime;
    }

    @Override
    public HttpStatus getStatusCode() {
        return this._statusCode;
    }

    @Override
    public String getErrorCode() {
        return this._errorCode;
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    @Override
    public ObjectNode toResponseFormat() {

        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("code", this._errorCode);
        error.put("message", this.getMessage());

        if (this._id > 0 && this._area != null && this._utcTime != null) {
            error.put("area", this._area);
            error.put("id", this._id);
            error.put("utcTime", this._utcTime);
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

        if (this._logContext != null) {
            error.set("context", this._logContext);
        }

        return error;
    }
}
