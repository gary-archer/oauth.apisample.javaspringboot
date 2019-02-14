package com.mycompany.api.basicapi.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javaync.io.AsyncFiles;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/*
 * A utility class to read the contents of a file into objects
 */
public class JsonFileReader {

    /*
     * Read data from a file into objects
     */
    public <T> CompletableFuture<T> readFile(String resourcePath, Class<T> runtimeType)
    {
        var mapper = this.createObjectMapper();
        var json = await(this.readJsonFromFile(resourcePath));

        try {
            return completedFuture(mapper.readValue(json, runtimeType));
        }
        catch(IOException ex) {
            throw new RuntimeException("IOException parsing JSON into an object", ex);
        }
    }

    /*
     * Create a mapper and potentially set serialization properties
     */
    private ObjectMapper createObjectMapper()
    {
        return new ObjectMapper();
    }

    /*
     * Do the work to read data from file
     */
    private CompletableFuture<String> readJsonFromFile(String resourcePath)
    {
        try {
            var url = getClass().getResource(resourcePath);
            if (url != null) {
                var uri = url.toURI();
                if (uri != null) {
                    var path = Paths.get(uri);
                    if (path != null) {
                        var bytes = await(AsyncFiles.readAllBytes(path));
                        return completedFuture(new String(bytes));
                    }
                }
            }
        }
        catch(URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException when reading JSON file", ex);
        }

        // We shouldn't get here but throw if the data was not found
        var message = String.format("The resource at path %s was not found", resourcePath);
        throw new RuntimeException(message);
    }
}
