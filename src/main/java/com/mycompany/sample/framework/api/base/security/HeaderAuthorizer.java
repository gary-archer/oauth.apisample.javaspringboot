package com.mycompany.sample.framework.api.base.security;

import org.springframework.beans.factory.BeanFactory;
import javax.servlet.http.HttpServletRequest;

/*
 * A simple authorizer for private subnet APIs, to receive claims via headers
 */
public final class HeaderAuthorizer extends BaseAuthorizer {

    /*
     * Receive our JSON configuration
     */
    public HeaderAuthorizer(final BeanFactory container) {
        super(container);
    }

    /*
     * Resolve the authenticator and ask it to read headers and return them as claims
     */
    @Override
    protected CoreApiClaims execute(final HttpServletRequest request) {

        var authenticator = super.getContainer().getBean(HeaderAuthenticator.class);
        return authenticator.authorizeRequestAndGetClaims(request);
    }
}
