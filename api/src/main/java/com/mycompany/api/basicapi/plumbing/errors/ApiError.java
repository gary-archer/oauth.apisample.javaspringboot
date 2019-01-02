package com.mycompany.api.basicapi.plumbing.errors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/*
 * An error entity that the API will log
 * Note that we avoid extra serialization of Lombok generated properties
 */
public class ApiError extends Exception {

    private static final Integer MIN_ERROR_ID = 10000;
    private static final Integer MAX_ERROR_ID = 99999;

    @Getter
    private final Integer statusCode;

    @Getter
    private final String area;

    @Getter
    private final Integer instanceId;

    @Getter
    private final String utcTime;

    @Getter
    @Setter
    private String url;

    @Getter
    @Setter
    private String details;

    /*
     * Construction
     */
    public ApiError(String area, String message)
    {
        super(message);
        this.area = area;

        // Generate an instance id
        this.instanceId = (int)Math.floor(Math.random() * (MAX_ERROR_ID - MIN_ERROR_ID + 1) + MIN_ERROR_ID);

        // Default other fields
        this.statusCode = 500;
        this.utcTime = Instant.now().toString();
    }

    /*
     * Return a dynamic object that can be serialized by calling toString
     */
    public ObjectNode toLogFormat() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode error = mapper.createObjectNode();
        error.put("area", this.area);
        error.put("message", this.getMessage());
        error.put("instanceId", this.instanceId);
        error.put("utcTime", this.utcTime);
        if(this.url != null)
        {
            error.put("url", this.url);
        }
        error.put("details", this.details);

        return error;
    }

    /*
     * Translate to a confidential error that is returned to the API caller, with a reference to the logged details
     */
    public ClientError toClientError()
    {
        ClientError error = new ClientError(this.statusCode, this.area, this.getMessage());
        error.setId(this.instanceId);
        return error;

    }
}
