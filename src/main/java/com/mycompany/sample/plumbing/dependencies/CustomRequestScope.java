package com.mycompany.sample.plumbing.dependencies;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/*
 * A custom request scope that stores its data in the HTTP request
 * This enables data to move across threads after an async await operation
 * This gives us a request scope equivalent to C# and NodeJS API technology stacks
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CustomRequestScope implements Scope {

    public static final String NAME = "CustomRequestScope";
    private final String keyName = "REQUEST_OBJECTS";

    /*
     * Get an object and add to the object map so that it is only created once
     */
    @Override
    public @NonNull Object get(
            final @NonNull String name,
            final @NonNull ObjectFactory<?> objectFactory) {

        System.out.println("GETTING OBJECT " + name);
        Map<String, Object> objectMap = this.getCurrentRequestObjects();
        if (!objectMap.containsKey(name)) {
            System.out.println("STORING OBJECT " + name);
            objectMap.put(name, objectFactory.getObject());
        }

        return objectMap.get(name);
    }

    /*
     * Remove an object from the object map at the end of an HTTP request
     */
    @Override
    public Object remove(final @NonNull String name) {

        System.out.println("REMOVING OBJECT " + name);
        Map<String, Object> objectMap = this.getCurrentRequestObjects();
        return objectMap.remove(name);
    }

    /*
     * We are not doing any object disposal
     */
    @Override
    public void registerDestructionCallback(
            final @NonNull String name,
            final @NonNull Runnable runnable) {
    }

    /*
     * This can be an empty implementation
     */
    @Override
    public Object resolveContextualObject(final @NonNull String name) {
        return null;
    }

    /*
     * Return a descriptive name
     */
    @Override
    public String getConversationId() {
        return "CustomRequestScope";
    }

    /*
     * Clear all objects at the end of an HTTP request
     */
    public void removeAll() {

        // Get the request object
        System.out.println("CLEARING ALL OBJECTS");
        var request = this.getCurrentRequest();

        // Get the data
        var data = request.getAttribute(this.keyName);
        if (data != null) {

            // Remove objects from the map
            var objectMap = (Map<String, Object>) data;
            System.out.println(String.format("OBJECT COUNT: %d", objectMap.size()));
            objectMap.clear();

            // Remove the attribute from the request
            request.removeAttribute(this.keyName);
        }
    }

    /*
     * Return the current HTTP request, which stores our state
     */
    private Map<String, Object> getCurrentRequestObjects() {

        // Get the request object
        var request = this.getCurrentRequest();

        // Get or create the data for injected objects
        var data = request.getAttribute(this.keyName);
        if (data == null) {
            System.out.println("CREATING OBJECT MAP");
            data = new HashMap<String, Object>();
            request.setAttribute(this.keyName, data);
        }

        // Return the object reference, to which items will be read or written
        return (Map<String, Object>) data;
    }

    /*
     * Use the static request attributes to get the current request
     */
    private HttpServletRequest getCurrentRequest() {

        // Use the static request attributes to get the current request
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }

        throw new RuntimeException("Unable to find current HTTP request");
    }
}
