package com.mycompany.sample.logic.utilities;

import org.springframework.aop.framework.Advised;

/*
 * During an API request that returns CompletableFuture there is a second ASYNC stage of the dispatcher
 * When injecting request scoped objects, CGLIB proxies try to resolve real objects again in the ASYNC stage
 * However, Spring uses thread local storage for request scoped objects, and the thread may have changed
 * This can lead to the below cryptic error, even though we are still in the context of a request
 * The CompanyRepository works around this by storing a reference to the real object and not the CGLIB proxy
 *
 *     Scope 'request' is not active for the current thread; consider defining a scoped proxy for this bean
 *     if you intend to refer to it from a singleton; nested exception is java.lang.IllegalStateException:
 *     No thread-bound request found: Are you referring to request attributes outside of an actual web request,
 *     or processing a request outside of the originally receiving thread? If you are actually operating within a
 *     web request and still receive this message, your code is probably running outside of DispatcherServlet:
 *     In this case, use RequestContextListener or RequestContextFilter to expose the current request.
 */
public final class CGLib {

    private CGLib() {
    }

    /*
     * Return the real object behind a CGLIB proxy
     */
    public static <T> T unproxy(final T obj) {


        if (obj instanceof Advised) {
            try {

                // Attempt to get the underlying object
                @SuppressWarnings("unchecked")
                var result = (T) ((Advised) obj).getTargetSource().getTarget();
                return result;

            } catch (Exception e) {

                // Report error details
                String message = String.format("Unable to unproxy object of type %s", obj.getClass().getSimpleName());
                throw new RuntimeException(message, e);
            }
        }

        return obj;
    }
}
