package com.mycompany.sample.plumbing.logging;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * Represents a time measurement within an API operation
 * These operations are exported and this interface can be used from business logic via the ILogEntry
 */
public interface PerformanceBreakdown extends AutoCloseable {

    // Set details to associate with the performance breakdown, such as SQL and parameters
    void setDetails(JsonNode value);

    // Create a child performance breakdown
    PerformanceBreakdown createChild(String name);

    // Override the default implementation to remove the throws clause and make try with resources easier to use
    // Our implementation's close method does not throw any checked exceptions so this is correct
    @Override
    void close();
}
