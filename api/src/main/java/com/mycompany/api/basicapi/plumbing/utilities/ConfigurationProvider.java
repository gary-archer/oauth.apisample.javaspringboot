package com.mycompany.api.basicapi.plumbing.utilities;

import com.mycompany.api.basicapi.configuration.Configuration;

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
