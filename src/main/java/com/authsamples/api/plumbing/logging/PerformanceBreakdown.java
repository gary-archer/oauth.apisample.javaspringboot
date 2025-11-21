package com.authsamples.api.plumbing.logging;

import tools.jackson.databind.JsonNode;

/*
 * Represents a time measurement within an API operation
 */
public interface PerformanceBreakdown extends AutoCloseable {

    // Set details to associate with the performance breakdown
    void setDetails(JsonNode value);

    // Create a child breakdown for an inner timing
    PerformanceBreakdown createChild(String name);

    // Override the default implementation to remove the throws clause and make try with resources easier to use
    @Override
    void close();
}
