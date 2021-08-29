package com.mycompany.sample.plumbing.interceptors;

import java.util.Map;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;

/*
 * Do custom logging of requests for support purposes
 */
public final class LoggingInterceptor implements HandlerInterceptor {

    private final BeanFactory container;

    public LoggingInterceptor(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Do pre request handling and handle errors properly, since by default Spring does not add CORS headers
     */
    @Override
    public boolean preHandle(
            final @NonNull HttpServletRequest request,
            final @NonNull HttpServletResponse response,
            final @NonNull Object handler) {

        try {

            // Only run the logging interceptor during the request stage and not in the async completion stage
            if (request.getDispatcherType().equals(DispatcherType.REQUEST)) {

                // Get the log entry for this request
                var logEntry = this.container.getBean(LogEntryImpl.class);

                // Call start, which will be a no-op if logging has already been started by the authorizer
                logEntry.start(request);

                // Record populated path segments here as the resource id
                @SuppressWarnings("unchecked")
                var pathVariables = (Map<String, String>)
                        request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                logEntry.setResourceId(pathVariables);
            }

        } catch (Exception filterException) {

            // Handle filter errors
            var exceptionHandler = this.container.getBean(UnhandledExceptionHandler.class);
            exceptionHandler.handleFilterException(request, response, filterException);
            return false;
        }

        return true;
    }

    /*
     * Do post request handling and handle errors properly, since by default Spring does not add CORS headers
     */
    @Override
    public void afterCompletion(
            final @NonNull HttpServletRequest request,
            final @NonNull HttpServletResponse response,
            final @NonNull Object handler,
            final Exception ex) {

        try {

            // Finish logging of successful requests
            var logEntry = this.container.getBean(LogEntryImpl.class);
            var handlerMappings = this.container.getBean(RequestMappingHandlerMapping.class);
            logEntry.end(request, response, handlerMappings);
            logEntry.write();

            // Clean up per request dependencies
            CustomRequestScope.removeAll();

        } catch (Exception filterException) {

            // Handle filter errors
            var exceptionHandler = this.container.getBean(UnhandledExceptionHandler.class);
            exceptionHandler.handleFilterException(request, response, filterException);
        }
    }
}
