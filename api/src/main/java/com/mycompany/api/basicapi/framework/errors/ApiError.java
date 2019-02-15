package com.mycompany.api.basicapi.framework.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.time.Instant;

/*
 * An error entity that the API will log
 */
public class ApiError extends RuntimeException {

    /*
     * A range for generated instance ids
     */
    private static final int MIN_ERROR_ID = 10000;
    private static final int MAX_ERROR_ID = 99999;

    /*
     * Error properties to log
     */
    private HttpStatus statusCode;
    private String errorCode;
    private final String area;
    private final int instanceId;
    private final String utcTime;

    @Setter
    private String details;

    /*
     * Construction
     */
    public ApiError(String errorCode, String userMessage)
    {
        super(userMessage);

        this.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        this.errorCode = errorCode;
        this.area = "BasicApi";
        this.instanceId = (int)Math.floor(Math.random() * (MAX_ERROR_ID - MIN_ERROR_ID + 1) + MIN_ERROR_ID);
        this.utcTime = Instant.now().toString();
        this.details = null;
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    public ObjectNode toLogFormat() {

        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("statusCode", this.statusCode.value());
        error.set("clientError", this.toClientError().toResponseFormat());

        var serviceError = mapper.createObjectNode();
        serviceError.put("errorCode", this.errorCode);
        serviceError.put("details", this.details);
        error.set("serviceError", serviceError);
        return error;
    }

    /*
     * Translate to a confidential error that is returned to the API caller, with a reference to the logged details
     */
    public ClientError toClientError()
    {
        // Set a generic client error code for the server exception
        var error = new ClientError(this.statusCode, "internal_server_error", this.getMessage());

        // Also indicate which part of the system, where in logs and when the error occurred
        error.setExceptionDetails(this.area, this.instanceId, this.utcTime);
        return error;
    }
}
