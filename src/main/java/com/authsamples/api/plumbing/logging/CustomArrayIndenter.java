package com.authsamples.api.plumbing.logging;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultPrettyPrinter;

/*
 * This ensures that we output exception stack traces on separate lines
 */
public final class CustomArrayIndenter implements DefaultPrettyPrinter.Indenter {

    @Override
    public void writeIndentation(final JsonGenerator jsonGenerator, final int currentIndent) {

        // Get a padded string with 2 spaces per indent level
        String indent = "  ".repeat(Math.max(0, currentIndent));

        // Write a new line and then the current indent level, after which the array item will be output
        jsonGenerator.writeRaw(System.lineSeparator());
        jsonGenerator.writeRaw(indent);
    }

    @Override
    public boolean isInline() {
        return false;
    }
}
