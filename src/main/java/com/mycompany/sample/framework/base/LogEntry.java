package com.mycompany.sample.framework.base;

import com.fasterxml.jackson.databind.JsonNode;
import com.mycompany.sample.framework.api.base.logging.ChildLogEntry;

/*
 * An abstraction that business logic can use to add to the current production log context
 */
public interface LogEntry {

    // Create a performance breakdown
    PerformanceBreakdown createPerformanceBreakdown(String name);

    // Add arbitrary data to include in logs
    void addInfo(JsonNode info);

    // Our sample logs OAuth authorization as a child log entry
    ChildLogEntry createChild(String name);
}
