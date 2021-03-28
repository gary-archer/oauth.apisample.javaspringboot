package com.mycompany.sample.plumbing.logging;

/*
 * A helper to support the dispose pattern for child operations
 */
public final class ChildLogEntry implements AutoCloseable {

    private final LogEntryImpl _logEntry;

    public ChildLogEntry(final LogEntryImpl logEntry) {
        this._logEntry = logEntry;
    }

    @Override
    public void close() {
        this._logEntry.endChildOperation();
    }
}
