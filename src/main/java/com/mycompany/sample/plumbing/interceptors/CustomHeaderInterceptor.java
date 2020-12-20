package com.mycompany.sample.plumbing.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.mycompany.sample.plumbing.errors.ErrorCodes;
import com.mycompany.sample.plumbing.errors.ErrorFactory;
import com.mycompany.sample.plumbing.utilities.RequestClassifier;

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
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    @Override
    public boolean preHandle(
        final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response,
        final @NonNull Object handler) {

        var requestClassifier = this.container.getBean(RequestClassifier.class);
        if (requestClassifier.isApiStartRequest(request)) {

            var apiToBreak = request.getHeader("x-mycompany-test-exception");
            if (!StringUtils.isEmpty(apiToBreak)) {
                if (apiToBreak.equalsIgnoreCase(this.apiName)) {
                    throw ErrorFactory.createServerError(
                        ErrorCodes.EXCEPTION_SIMULATION, "An exception was simulated in the API");

                }
            }
        }

        return true;
    }
}
