package com.mycompany.sample.framework.api.oauth.errors;

import com.fasterxml.jackson.databind.node.TextNode;
import com.mycompany.sample.framework.api.base.errors.ApiError;
import com.mycompany.sample.framework.api.base.errors.BaseErrorCodes;
import com.mycompany.sample.framework.api.base.errors.ClientError;
import com.mycompany.sample.framework.api.base.errors.ErrorFactory;
import com.nimbusds.oauth2.sdk.ErrorObject;
import org.javatuples.Pair;
import org.springframework.util.StringUtils;

/*
 * A class for handling exceptions, logging them and returning an error to the caller
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    /*
     * Return an error during metadata lookup
     */
    public static ApiError fromMetadataError(final Throwable ex, final String url) {

        var apiError = ErrorFactory.createApiError(
                OAuthErrorCodes.METADATA_LOOKUP_FAILURE,
                "Metadata lookup failed", ex);
        ErrorUtils.setErrorDetails(apiError, null, ex, url);
        return apiError;
    }

    /*
     * Handle introspection errors in the response body
     */
    public static ApiError fromIntrospectionError(final ErrorObject errorObject, final String url) {

        // Create the error
        var oauthError = ErrorUtils.readOAuthErrorResponse(errorObject);
        var apiError = createOAuthApiError(
                OAuthErrorCodes.INTROSPECTION_FAILURE,
                "Token validation failed",
                oauthError.getValue0());

        // Set technical details
        ErrorUtils.setErrorDetails(apiError, oauthError.getValue1(), null, url);
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ApiError fromIntrospectionError(final Throwable ex, final String url) {

        // Already handled from response data
        if (ex instanceof ApiError) {
            return (ApiError) ex;
        }

        // Already handled due to invalid token
        if (ex instanceof ClientError) {
            throw (ClientError) ex;
        }

        var apiError = ErrorFactory.createApiError(
                OAuthErrorCodes.INTROSPECTION_FAILURE,
                "Token validation failed", ex);
        ErrorUtils.setErrorDetails(apiError, null, ex, url);
        return apiError;
    }

    /*
     * Handle user info errors in the response body
     */
    public static ApiError fromUserInfoError(final ErrorObject errorObject, final String url) {

        // Create the error
        var oauthError = ErrorUtils.readOAuthErrorResponse(errorObject);
        var apiError = createOAuthApiError(
                OAuthErrorCodes.USERINFO_FAILURE,
                "User info lookup failed", oauthError.getValue0());

        // Set technical details
        ErrorUtils.setErrorDetails(apiError, oauthError.getValue1(), null, url);
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public static ApiError fromUserInfoError(final Throwable ex, final String url) {

        // Already handled from response data
        if (ex instanceof ApiError) {
            return (ApiError) ex;
        }

        // Already handled due to invalid token
        if (ex instanceof ClientError) {
            throw (ClientError) ex;
        }

        var apiError = ErrorFactory.createApiError(OAuthErrorCodes.USERINFO_FAILURE, "User info lookup failed", ex);
        ErrorUtils.setErrorDetails(apiError, null, ex, url);
        return apiError;
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public static ApiError fromMissingClaim(final String claimName) {

        var apiError = ErrorFactory.createApiError(BaseErrorCodes.CLAIMS_FAILURE, "Authorization data not found");
        var message = String.format("An empty value was found for the expected claim %s", claimName);
        apiError.setDetails(new TextNode(message));
        return apiError;
    }

    /*
     * Return the error and error_description fields from an OAuth error message
     */
    private static Pair<String, String> readOAuthErrorResponse(final ErrorObject errorObject) {

        String code = null;
        String description = null;
        if (errorObject != null) {

            if (!StringUtils.isEmpty(errorObject.getCode())) {
                code = errorObject.getCode();
            }

            if (!StringUtils.isEmpty(errorObject.getDescription())) {
                description = errorObject.getDescription();
            }
        }

        return Pair.with(code, description);
    }

    /*i
     * Create an error object from an error code and include the OAuth error code in the user message
     */
    private static ApiError createOAuthApiError(
            final String errorCode,
            final String userMessage,
            final String oauthErrorCode) {

        // Include the OAuth error code in the short technical message returned
        String message = userMessage;
        if (!StringUtils.isEmpty(oauthErrorCode)) {
            message += String.format(" : %s", oauthErrorCode);
        }

        return ErrorFactory.createApiError(errorCode, message);
    }

    /*
     * Update the API error object with technical exception details
     */
    private static void setErrorDetails(
            final ApiError error,
            final String oauthDetails,
            final Throwable ex,
            final String url) {

        var detailsText = "";
        if (!StringUtils.isEmpty(oauthDetails)) {
            detailsText += oauthDetails;
        } else if (ex != null) {
            detailsText += ErrorUtils.getExceptionDetailsMessage(ex);
        }

        if (!StringUtils.isEmpty(url)) {
            detailsText += String.format(", URL: %s", url);
        }

        error.setDetails(new TextNode(detailsText));
    }

    /*
     * Set a string version of the exception against the API error, which will be logged
     */
    private static String getExceptionDetailsMessage(final Throwable ex) {

        if (ex.getMessage() == null) {
            return String.format("%s", ex.getClass());
        } else {
            return String.format("%s", ex.getMessage());
        }
    }
}
