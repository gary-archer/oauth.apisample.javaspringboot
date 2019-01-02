package com.mycompany.api.basicapi.plumbing.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import java.util.Optional;

/*
 * An error entity to return to the caller
 */
public class ClientError extends Exception {

    @Getter
    private final Integer statusCode;

    @Getter
    private final String area;

    @Getter
    private Optional<Integer> id;

    /*
     * Construct from a status code
     */
    public ClientError(Integer statusCode, String area, String message)
    {
        super(message);
        this.statusCode = statusCode;
        this.area = area;
        this.id = Optional.empty();
    }

    /*
     * When there is a 500 this is called by the exception handler
     */
    public void setId(Integer instanceId)
    {
        this.id = Optional.of(instanceId);
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    public ObjectNode toResponseFormat()
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();
        error.put("area", this.area);
        error.put("message", this.getMessage());

        if(this.id.isPresent()) {
            error.put("id", this.id.get());
        }

        return error;
    }

    /*
     * Similar to the above but includes the status code
     */
    public ObjectNode ToLogFormat()
    {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();
        error.put("statusCode", this.statusCode);
        error.set("body", this.toResponseFormat());
        return error;
    }
}
