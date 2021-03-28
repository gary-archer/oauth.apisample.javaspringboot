package com.mycompany.sample.plumbing.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

/*
 * The full implementation class
 */
public final class PerformanceBreakdownImpl implements PerformanceBreakdown {

    private String _name;
    private Instant _startTime;
    private ArrayList<PerformanceBreakdownImpl> _children;
    private JsonNode _details;

    @Getter
    @Setter
    private long millisecondsTaken;

    /*
     * Set defaults for fields
     */
    public PerformanceBreakdownImpl(final String name) {
        this._name = name;
        this.millisecondsTaken = 0;
        this._details = null;
        this._children = new ArrayList<>();
    }

    /*
     * Start measuring
     */
    public void start() {
        this._startTime = Instant.now();
    }

    /*
     * An overload that supports more detailed data
     */
    public void setDetails(final JsonNode value) {
        this._details = value;
    }

    /*
     * Stop the timer and finish the measurement, converting nanoseconds to milliseconds
     */
    @Override
    public void close() {
        this.millisecondsTaken = Duration.between(this._startTime, Instant.now()).toMillis();
    }

    /*
     * Return the performance data in the output format
     */
    public ObjectNode getData() {

        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();

        data.put("name", this._name);
        data.put("millisecondsTaken", this.millisecondsTaken);

        if (this._details != null) {
            data.set("details", this._details);
        }

        if (this._children.size() > 0) {

            var childNodes = mapper.createArrayNode();
            for (var child : this._children) {
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
    public PerformanceBreakdown createChild(final String name) {

        var child = new PerformanceBreakdownImpl(name);
        this._children.add(child);
        child.start();
        return child;
    }
}
