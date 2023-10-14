package com.mycompany.sample.logic.utilities;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
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

        return this.readJsonFromFile(resourcePath).thenCompose(json -> {

            try {
                var mapper = new ObjectMapper();
                return completedFuture(mapper.readValue(json, runtimeType));

            } catch (Throwable mapException) {

                throw ErrorFactory.createServerError(
                        SampleErrorCodes.FILE_READ_ERROR,
                        "Problem encountered deserializing file data",
                        mapException);
            }
        });
    }

    /*
     * Do the work to read data from file
     */
    private CompletableFuture<String> readJsonFromFile(final String filePath) {

        try {

            var path = Paths.get(filePath);
            return AsyncFiles.readAllBytes(path).thenCompose(bytes -> completedFuture(new String(bytes)));

        } catch (Throwable ex) {

            throw ErrorFactory.createServerError(
                    SampleErrorCodes.FILE_READ_ERROR,
                    "Problem encountered reading file data",
                    ex);
        }
    }
}
