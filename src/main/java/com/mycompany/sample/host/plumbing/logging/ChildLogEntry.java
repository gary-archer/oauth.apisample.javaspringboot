package com.mycompany.sample.host.plumbing.logging;

/*
 * A helper to support the dispose pattern for child operations
 */
public final class ChildLogEntry implements AutoCloseable {

    private final LogEntryImpl logEntry;

    public ChildLogEntry(final LogEntryImpl logEntry) {
        this.logEntry = logEntry;
    }

    @Override
    public void close() {
        this.logEntry.endChildOperation();
    }
}
