package com.mycompany.api.basicapi.plumbing.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javaync.io.AsyncFiles;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/*
 * A utility class to read the contents of a file into objects
 */
@Repository
@RequestScope
public class JsonFileReader {

    public JsonFileReader() {
        System.out.println("***** CREATE FILE READER");
    }

    public void SayHello(String message) {
        System.out.println("*** JSON file reader: " + message);
    }

    /*
     * Read data from a file into objects
     */
    public <T> CompletableFuture<T> ReadFile(String resourcePath, Class<T> runtimeType)
    {
        System.out.println("***** IN READ FILE");

        ObjectMapper mapper = this.CreateObjectMapper();
        String json = await(this.ReadJsonFromFile(resourcePath));

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
    private ObjectMapper CreateObjectMapper()
    {
        return new ObjectMapper();
    }

    /*
     * Do the work to read data from file
     */
    private CompletableFuture<String> ReadJsonFromFile(String resourcePath)
    {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                URI uri = url.toURI();
                if (uri != null) {
                    Path path = Paths.get(uri);
                    if (path != null) {
                        byte[] bytes = await(AsyncFiles.readAllBytes(path));
                        return completedFuture(new String(bytes));
                    }
                }
            }
        }
        catch(URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException when reading JSON file", ex);
        }

        // We shouldn't get here but throw if the data was not found
        String message = String.format("The resource at path %s was not found", resourcePath);
        throw new IllegalStateException(message);
    }
}
