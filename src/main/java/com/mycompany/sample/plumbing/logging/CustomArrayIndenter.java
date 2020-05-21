package com.mycompany.sample.plumbing.logging;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

/*
 * This ensures that we output exception stack traces on separate lines
 */
public final class CustomArrayIndenter implements DefaultPrettyPrinter.Indenter {

    @Override
    public void writeIndentation(final JsonGenerator jsonGenerator, final int currentIndent) throws IOException {

        // Get a padded string with 2 spaces per indent level
        var builder = new StringBuilder();
        for (int count = 0; count < currentIndent; count++) {
            builder.append("  ");
        }

        // Write a new line and then the current indent level, after which the array item will be output
        jsonGenerator.writeRaw(System.lineSeparator());
        jsonGenerator.writeRaw(builder.toString());
    }

    @Override
    public boolean isInline() {
        return false;
    }
}
