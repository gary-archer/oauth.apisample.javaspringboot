package com.mycompany.sample.host.plumbing.logging;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * An abstraction that logic classes can use to add to the current production log context
 */
public interface LogEntry {

    // Create a performance breakdown
    PerformanceBreakdown createPerformanceBreakdown(String name);

    // Add arbitrary data to include in logs
    void addInfo(JsonNode info);

    // Our sample logs OAuth authorization as a child log entry
    ChildLogEntry createChild(String name);
}
