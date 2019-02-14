package com.mycompany.api.basicapi.utilities;

import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.utilities.JsonFileReader;

/*
 * Manage loading our configuration from a JSON file
 */
public class ConfigurationProvider {

    /*
     * Read the JSON configuration file into objects, waiting for the reader's future to complete synchronously
     */
    public Configuration Load()
    {
        var reader = new JsonFileReader();
        return reader.readFile("/api.config.json", Configuration.class)
                    .join();
    }
}
