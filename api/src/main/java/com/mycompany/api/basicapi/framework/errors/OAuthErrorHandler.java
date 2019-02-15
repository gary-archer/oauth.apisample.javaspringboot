package com.mycompany.api.basicapi.framework.errors;

import com.nimbusds.oauth2.sdk.ErrorObject;
import org.javatuples.Pair;
import org.springframework.util.StringUtils;

/*
 * A class for handling exceptions, logging them and returning an error to the caller
 */
public class OAuthErrorHandler extends BaseErrorHandler {

    /*
     * Return an error during metadata lookup
     */
    public ApiError fromMetadataError(Exception ex, String url) {

        var apiError = new ApiError("metadata_lookup_failure", "Metadata lookup failed");
        apiError.setDetails(this.getErrorDetails(this.getExceptionDetails(ex), url));
        return apiError;
    }

    /*
     * Handle introspection errors in the response body
     */
    public ApiError fromIntrospectionError(ErrorObject errorObject, String url) {

        // Create the error
        var oauthError = getOAuthErrorDetails(errorObject);
        var apiError = createOAuthApiError("introspection_failure", "Token validation failed", oauthError.getValue0());

        // Set technical details
        apiError.setDetails(this.getErrorDetails(oauthError.getValue1(), url));
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public ApiError fromIntrospectionError(Exception ex, String url) {

        // Already handled from response data
        if(ex instanceof ApiError) {
            return (ApiError)ex;
        }

        var apiError = new ApiError("introspection_failure", "Token validation failed");
        apiError.setDetails(this.getErrorDetails(this.getExceptionDetails(ex), url));
        return apiError;
    }

    /*
     * Handle user info errors in the response body
     */
    public ApiError fromUserInfoError(ErrorObject errorObject, String url) {

        // Create the error
        var oauthError = getOAuthErrorDetails(errorObject);
        var apiError = createOAuthApiError("userinfo_failure", "User info lookup failed", oauthError.getValue0());

        // Set technical details
        apiError.setDetails(this.getErrorDetails(oauthError.getValue1(), url));
        return apiError;
    }

    /*
     * Handle exceptions during user info lookup
     */
    public ApiError fromUserInfoError(Exception ex, String url) {

        // Already handled from response data
        if(ex instanceof ApiError) {
            return (ApiError)ex;
        }

        var apiError = new ApiError("userinfo_failure", "User info lookup failed");
        apiError.setDetails(this.getErrorDetails(this.getExceptionDetails(ex), url));
        return apiError;
    }

    /*
     * The error thrown if we cannot find an expected claim during OAuth processing
     */
    public ApiError fromMissingClaim(String claimName) {

        var apiError = new ApiError("claims_failure", "Authorization data not found");
        apiError.setDetails(String.format("An empty value was found for the expected claim %s", claimName));
        return apiError;
    }

    /*
     * Return the error and error_description fields from an OAuth error message
     */
    private Pair<String, String> getOAuthErrorDetails(ErrorObject errorObject) {

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

    /*
     * Create an error object from an error code and include the OAuth error code in the user message
     */
    private ApiError createOAuthApiError(String errorCode, String userMessage, String oauthErrorCode) {

        // Include the OAuth error code in the short technical message returned
        String message = userMessage;
        if (!StringUtils.isEmpty(errorCode)) {
            message += String.format(" : %s", errorCode);
        }

        return new ApiError(errorCode, message);
    }

    /*
     * Concatenate parts of an error
     */
    private String getErrorDetails(String details, String url) {


        var detailsText = "";
        if (!StringUtils.isEmpty(details)) {
            detailsText += details;
        }

        if(!StringUtils.isEmpty(url)) {
            detailsText += String.format(", URL: %s", url);
        }

        return detailsText;
    }
}