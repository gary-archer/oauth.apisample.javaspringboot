package com.mycompany.sample.plumbing.interceptors;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.mycompany.sample.plumbing.logging.LogEntryImpl;
import com.mycompany.sample.plumbing.utilities.RequestClassifier;

/*
 * Do custom logging of requests for support purposes
 */
public final class LoggingInterceptor extends HandlerInterceptorAdapter {

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

            var requestClassifier = this.container.getBean(RequestClassifier.class);
            if (requestClassifier.isApiStartRequest(request)) {

                // Get the log entry for this request
                var logEntry = this.container.getBean(LogEntryImpl.class);

                // Get metadata for this request
                var handlerMappings = this.container.getBean(RequestMappingHandlerMapping.class);

                // Call start, which will be a no-op if logging has already been started by the authorizer
                logEntry.start(request, handlerMappings);

                // Set the resource ids at this stage, since it is not available in the authorizer
                @SuppressWarnings("unchecked")
                var pathVariables = (Map<String, String>)
                        request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                logEntry.setResourceId(pathVariables);
                return true;
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
     * Do post reqeest handling and handle errors properly, since by default Spring does not add CORS headers
     */
    @Override
    public void afterCompletion(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final Exception ex) {

        try {

            var requestClassifier = this.container.getBean(RequestClassifier.class);
            if (requestClassifier.isApiRequest(request)) {
                var logEntry = this.container.getBean(LogEntryImpl.class);
                logEntry.end(response);
                logEntry.write();
            }

        } catch (Exception filterException) {

            // Handle filter errors
            var exceptionHandler = this.container.getBean(UnhandledExceptionHandler.class);
            exceptionHandler.handleFilterException(request, response, filterException);
        }
    }
}
