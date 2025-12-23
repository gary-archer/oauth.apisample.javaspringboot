package com.authsamples.api.plumbing.interceptors;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.logging.LogEntryImpl;
import com.authsamples.api.plumbing.logging.LoggerFactory;

/*
 * Do custom logging of requests for support purposes
 */
public final class LoggingInterceptor implements HandlerInterceptor {

    private final BeanFactory container;
    private final LoggerFactory loggerFactory;

    public LoggingInterceptor(final BeanFactory container, final LoggerFactory loggerFactory) {
        this.container = container;
        this.loggerFactory = loggerFactory;
    }

    /*
     * Do pre request handling to ensure reliability
     */
    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler) {

        try {

            // Get the log entry for this request
            var logEntry = this.container.getBean(LogEntryImpl.class);

            // Call start, which will be a no-op if logging has already been started by the authorizer
            logEntry.start(request);

            // Record populated path segments here as the resource id
            @SuppressWarnings("unchecked")
            var pathVariables = (Map<String, String>)
                    request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            logEntry.setResourceId(pathVariables);

        } catch (Exception filterException) {

            // Handle filter errors
            var exceptionHandler = this.container.getBean(UnhandledExceptionHandler.class);
            exceptionHandler.handleFilterException(request, response, filterException);
            return false;
        }

        return true;
    }

    /*
     * Do post request handling and complete logging
     */
    @Override
    public void afterCompletion(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler,
            final Exception ex) {

        try {

            // Finish logging of successful requests
            var logEntry = this.container.getBean(LogEntryImpl.class);
            var handlerMappings = this.container.getBean(RequestMappingHandlerMapping.class);
            logEntry.end(request, response, handlerMappings);

            // Output the request log
            var requestLogger = loggerFactory.getRequestLogger();
            if (requestLogger != null) {
                requestLogger.info("info", logEntry.getRequestLog());
            }

            // Output the audit log
            var auditLogger = loggerFactory.getAuditLogger();
            if (auditLogger != null) {
                auditLogger.info("info", logEntry.getAuditLog());
            }

            // Clean up per request dependencies
            CustomRequestScope.removeAll();

        } catch (Exception filterException) {

            // Handle filter errors
            var exceptionHandler = this.container.getBean(UnhandledExceptionHandler.class);
            exceptionHandler.handleFilterException(request, response, filterException);
        }
    }
}
