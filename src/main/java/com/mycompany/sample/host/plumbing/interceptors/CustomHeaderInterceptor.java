package com.mycompany.sample.host.plumbing.interceptors;

import com.mycompany.sample.host.plumbing.errors.ErrorCodes;
import com.mycompany.sample.host.plumbing.errors.ErrorFactory;
import com.mycompany.sample.host.plumbing.utilities.RequestClassifier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/*
 * A class to process custom headers to enable testers to control non functional behaviour
 */
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CustomHeaderInterceptor extends HandlerInterceptorAdapter {

    private final BeanFactory container;
    private final String apiName;

    public CustomHeaderInterceptor(final BeanFactory container, final String apiName) {
        this.container = container;
        this.apiName = apiName;
    }

    /*
     * Do pre request handling and handle errors properly, since by default Spring does not add CORS headers
     */
    @Override
    public boolean preHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler) {

        var requestClassifier = this.container.getBean(RequestClassifier.class);
        if (requestClassifier.isApiStartRequest(request)) {

            var apiToBreak = request.getHeader("x-mycompany-test-exception");
            if (!StringUtils.isEmpty(apiToBreak)) {
                if (apiToBreak.toLowerCase().equals(this.apiName.toLowerCase())) {
                    throw ErrorFactory.createServerError(
                        ErrorCodes.EXCEPTION_SIMULATION, "An exception was simulated in the API");

                }
            }
        }

        return true;
    }
}
