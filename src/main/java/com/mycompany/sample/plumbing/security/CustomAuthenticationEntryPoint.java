package com.mycompany.sample.plumbing.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import com.mycompany.sample.plumbing.interceptors.UnhandledExceptionHandler;

/*
 * A class to control the response returned to the client when authorization fails
 */
public final class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final BeanFactory container;

    public CustomAuthenticationEntryPoint(final BeanFactory container) {
        this.container = container;
    }

    /*
     * Write a response with an error object and include CORS headers, which are omitted by default
     * This ensures that our SPA client can correctly handle the response
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException ex) {

        var handler = this.container.getBean(UnhandledExceptionHandler.class);
        handler.handleFilterException(request, response, ex);
    }
}
