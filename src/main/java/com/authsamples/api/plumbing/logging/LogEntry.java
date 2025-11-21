package com.authsamples.api.plumbing.logging;

import tools.jackson.databind.JsonNode;

/*
 * A log entry collects data during an API request and outputs it at the end
 */
public interface LogEntry {

    // Create a performance breakdown
    PerformanceBreakdown createPerformanceBreakdown(String name);

    // Add arbitrary data to include in logs
    void addInfo(JsonNode info);
}
