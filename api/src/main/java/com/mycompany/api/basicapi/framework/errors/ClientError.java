package com.mycompany.api.basicapi.framework.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/*
 * An error entity to return to the caller
 */
public class ClientError extends RuntimeException {

    @Getter
    private final HttpStatus statusCode;
    private final String errorCode;

    private String area;
    private int id;
    private String utcTime;


    /*
     * Construct from mandatory fields
     */
    public ClientError(HttpStatus statusCode, String errorCode, String message)
    {
        // Set mandatory fields
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;

        // Initialise 5xx fields
        this.area = null;
        this.id = 0;
        this.utcTime = null;
    }

    /*
     * When there is a 500 this is called by the exception handler
     */
    public void setExceptionDetails(String area, int instanceId, String utcTime)
    {
        this.area = area;
        this.id = instanceId;
        this.utcTime = utcTime;
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    public ObjectNode toResponseFormat()
    {
        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("code", this.errorCode);
        error.put("message", this.getMessage());

        if(this.id > 0 && this.area != null && this.utcTime != null) {
            error.put("area", this.area);
            error.put("id", this.id);
            error.put("utcTime", this.utcTime);
        }

        return error;
    }

    /*
     * Similar to the above but includes the status code
     */
    public ObjectNode ToLogFormat()
    {
        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("statusCode", this.statusCode.value());
        error.set("body", this.toResponseFormat());
        return error;
    }
}
