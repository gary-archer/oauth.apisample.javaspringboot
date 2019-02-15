package com.mycompany.api.basicapi.utilities;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
 * A singleton that can be injected into controllers and used to create request scoped objects
 * This avoids problems when accessing the same object after async completion, when the dispatcher type moves from REQUEST to ASYNC
 * This could be extracted behind an interface for testing
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RequestScopeObjectFactory {

    /*
     * The factory is injected into the repository constructor, which then creates a file reader
     * The file reader is then usable across async calls, by different threads
     */
    public JsonFileReader createJsonFileReader() {
        return new JsonFileReader();
    }
}
