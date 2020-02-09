package com.mycompany.sample.framework.api.base.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.framework.base.PerformanceBreakdown;
import lombok.Getter;
import lombok.Setter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

/*
 * The full implementation class is private to the framework
 */
public final class PerformanceBreakdownImpl implements PerformanceBreakdown {

    private String name;
    private Instant startTime;
    private ArrayList<PerformanceBreakdownImpl> children;
    private JsonNode details;

    @Getter
    @Setter
    private long millisecondsTaken;

    /*
     * Set defaults for fields
     */
    public PerformanceBreakdownImpl(final String name) {
        this.name = name;
        this.millisecondsTaken = 0;
        this.details = null;
        this.children = new ArrayList<PerformanceBreakdownImpl>();
    }

    /*
     * Start measuring
     */
    public void start() {
        this.startTime = Instant.now();
    }

    /*
     * An overload that supports more detailed data
     */
    @Override
    public void setDetails(final JsonNode value) {
        this.details = value;
    }

    /*
     * Stop the timer and finish the measurement, converting nanoseconds to milliseconds
     */
    @Override
    public void close() {
        this.millisecondsTaken = Duration.between(this.startTime, Instant.now()).toMillis();
    }

    /*
     * Return the performance data in the output format
     */
    public ObjectNode getData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        data.put("name", this.name);
        data.put("millisecondsTaken", this.millisecondsTaken);

        if (this.details != null) {
            data.set("details", this.details);
        }

        if (this.children.size() > 0) {

            var childNodes = mapper.createArrayNode();
            for (var child : this.children) {
                childNodes.add(child.getData());
            }

            data.set("children", childNodes);
        }

        // Return the JSON string to log
        return data;
    }

    /*
     * Add a child to the performance breakdown
     */
    public PerformanceBreakdownImpl createChild(final String name) {

        var child = new PerformanceBreakdownImpl(name);
        this.children.add(child);
        return child;
    }
}
