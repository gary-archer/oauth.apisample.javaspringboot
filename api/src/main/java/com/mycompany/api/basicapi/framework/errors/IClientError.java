package com.mycompany.api.basicapi.framework.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;

/*
 * An interface that all types of client error object support
 */
public interface IClientError {

    // Return the HTTP status code
    HttpStatus getStatusCode();

    // Return the JSON response format
    ObjectNode toResponseFormat();

    // Return the log format
    default ObjectNode toLogFormat()
    {
        var mapper = new ObjectMapper();
        var error = mapper.createObjectNode();
        error.put("statusCode", this.getStatusCode().value());
        error.set("body", this.toResponseFormat());
        return error;
    }
}
