package com.authsamples.api.plumbing.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import com.authsamples.api.plumbing.errors.BaseErrorCodes;
import com.authsamples.api.plumbing.errors.ErrorFactory;

/*
 * A class to process custom headers to enable testers to control non functional behaviour
 */
public final class CustomHeaderInterceptor implements HandlerInterceptor {

    private final String apiName;

    public CustomHeaderInterceptor(final String apiName) {
        this.apiName = apiName;
    }

    /*
     * Check for a known custom header and throw an exception if required
     */
    @Override
    public boolean preHandle(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler) {

        var apiToBreak = request.getHeader("authsamples-test-exception");
        if (StringUtils.hasLength(apiToBreak)) {
            if (apiToBreak.equalsIgnoreCase(this.apiName)) {
                throw ErrorFactory.createServerError(
                    BaseErrorCodes.EXCEPTION_SIMULATION, "An exception was simulated in the API");

            }
        }

        return true;
    }
}
