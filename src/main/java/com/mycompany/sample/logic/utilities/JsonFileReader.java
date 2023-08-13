package com.mycompany.sample.logic.utilities;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.javaync.io.AsyncFiles;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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

        Function<String, CompletableFuture<T>> callback = json -> {

            try {
                var mapper = this.createObjectMapper();
                return completedFuture(mapper.readValue(json, runtimeType));

            } catch (Throwable mapException) {

                throw ErrorFactory.createServerError(
                        SampleErrorCodes.FILE_READ_ERROR,
                        "Problem encountered mapping file data",
                        mapException);
            }
        };

        return this.readJsonFromFile(resourcePath)
           .thenCompose(callback);
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

        Function<byte[], CompletableFuture<String>> callback = bytes ->
            completedFuture(new String(bytes));

        try {

            var path = Paths.get(filePath);
            return AsyncFiles.readAllBytes(path).thenCompose(callback);

        } catch (Throwable ex) {

            throw ErrorFactory.createServerError(
                    SampleErrorCodes.FILE_READ_ERROR,
                    "Problem encountered reading file data",
                    ex);
        }
    }
}
