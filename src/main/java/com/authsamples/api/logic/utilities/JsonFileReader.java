package com.authsamples.api.logic.utilities;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.authsamples.api.logic.errors.ErrorCodes;
import com.authsamples.api.plumbing.errors.ErrorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * A utility class to manage async reading of JSON text file data into objects
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonFileReader {

    /*
     * Read data from a file into objects
     */
    public <T> T readFile(final String resourcePath, final Class<T> runtimeType) {

        var json = this.readJsonFromFile(resourcePath);
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(json, runtimeType);

        } catch (Throwable mapException) {

            throw ErrorFactory.createServerError(
                    ErrorCodes.FILE_READ_ERROR,
                    "Problem encountered deserializing file data",
                    mapException);
        }
    }

    /*
     * Do the work to read data from file, which will block on a virtual thread
     * The main thread of execution returns to the thread pool and handles further requests
     */
    private String readJsonFromFile(final String filePath) {

        try {

            var path = Paths.get(filePath);
            var bytes = Files.readAllBytes(path);
            return new String(bytes);

        } catch (Throwable ex) {

            throw ErrorFactory.createServerError(
                    ErrorCodes.FILE_READ_ERROR,
                    "Problem encountered reading file data",
                    ex);
        }
    }
}
