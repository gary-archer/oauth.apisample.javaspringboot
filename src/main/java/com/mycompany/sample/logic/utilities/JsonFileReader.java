package com.mycompany.sample.logic.utilities;

import java.nio.file.Paths;
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
    public <T> T readFile(final String resourcePath, final Class<T> runtimeType) {

        var json = this.readJsonFromFile(resourcePath);
        try {
            var mapper = new ObjectMapper();
            return mapper.readValue(json, runtimeType);

        } catch (Throwable mapException) {

            throw ErrorFactory.createServerError(
                    SampleErrorCodes.FILE_READ_ERROR,
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
            var bytes = AsyncFiles.readAllBytes(path).join();
            return new String(bytes);

        } catch (Throwable ex) {

            throw ErrorFactory.createServerError(
                    SampleErrorCodes.FILE_READ_ERROR,
                    "Problem encountered reading file data",
                    ex);
        }
    }
}
