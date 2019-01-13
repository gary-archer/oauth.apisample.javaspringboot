package com.mycompany.api.basicapi.plumbing.startup;

import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.plumbing.utilities.JsonFileReader;

/*
 * Manage loading our configuration from a JSON file
 */
public class ConfigurationProvider {

    /*
     * Read the JSON configuration file into objects, waiting for the reader's future to complete synchronously
     */
    public Configuration Load()
    {
        JsonFileReader reader = new JsonFileReader();
        return reader.ReadFile("/data/api.config.json", Configuration.class)
                    .join();
    }
}
