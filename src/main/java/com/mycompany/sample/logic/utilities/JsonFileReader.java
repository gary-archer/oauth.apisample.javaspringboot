package com.mycompany.sample.logic.utilities;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import org.javaync.io.AsyncFiles;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import static com.ea.async.Async.await;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.sample.logic.errors.SampleErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * A utility class to manage async reading of JSON text file data into objects
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class JsonFileReader {

    /*
     * Read data from a file into objects
     */
    public <T> CompletableFuture<T> readFile(final String resourcePath, final Class<T> runtimeType) {

        try {

            // Do the reading and deserialization
            var json = await(this.readJsonFromFile(resourcePath));
            var mapper = this.createObjectMapper();
            var data = mapper.readValue(json, runtimeType);
            return completedFuture(data);

        } catch (Throwable ex) {

            // Report the error including an error code and exception details
            throw ErrorFactory.createServerError(
                    SampleErrorCodes.FILE_READ_ERROR,
                    "Problem encountered reading data",
                    ex);
        }
    }

    /*
     * Create a mapper and potentially set serialization properties
     */
    private ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    /*
     * Do the work to read data from file
     */
    private CompletableFuture<String> readJsonFromFile(final String filePath) {

        // Try to load the file
        var path = Paths.get(filePath);
        if (path != null) {
            var bytes = await(AsyncFiles.readAllBytes(path));
            return completedFuture(new String(bytes));
        }

        // We shouldn't get here but throw if the data was not found
        var message = String.format("The resource at path %s was not found", filePath);
        throw new IllegalStateException(message);
    }
}
