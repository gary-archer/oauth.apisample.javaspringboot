package com.authsamples.api.middleware.errors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/*
 * A class to process custom headers to enable testers to control non functional behaviour
 */
public final class CustomHeaderInterceptor implements HandlerInterceptor {

    private final String apiName;

    public CustomHeaderInterceptor(String apiName) {
        this.apiName = apiName;
    }

    /*
     * Check for a known custom header and throw an exception if required
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        var apiToBreak = request.getHeader("api-exception-simulation");
        if (StringUtils.hasLength(apiToBreak)) {
            if (apiToBreak.equalsIgnoreCase(this.apiName)) {
                throw ErrorFactory.createServerError(
                    ErrorCodes.EXCEPTION_SIMULATION, "An exception was simulated in the API");

            }
        }

        return true;
    }
}
