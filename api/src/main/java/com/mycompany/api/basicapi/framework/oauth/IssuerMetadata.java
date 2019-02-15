package com.mycompany.api.basicapi.framework.oauth;

import com.mycompany.api.basicapi.framework.errors.OAuthErrorHandler;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.Getter;

/*
 * A class to download Open Id Connect metadata at application startup
 */
public class IssuerMetadata {

    /*
     * The application configuration
     */
    private final OauthConfiguration configuration;

    /*
     * Metadata is downloaded at application startup
     */
    @Getter
    private OIDCProviderMetadata metadata;

    /*
     * Receive the configuration at startup
     */
    public IssuerMetadata(OauthConfiguration configuration) {

        this.configuration = configuration;
    }

    /*
     * Read the metadata here
     */
    public void initialize(){

        try {
            // Create the issuer object
            var issuer = new Issuer(this.configuration.getAuthority());

            // Make the HTTP request for metadata
            var request = new OIDCProviderConfigurationRequest(issuer).toHTTPRequest();
            HTTPResponse response = request.send();
            this.metadata = OIDCProviderMetadata.parse(response.getContentAsJSONObject());

        } catch(Exception e) {

            // Throw an understandable error
            var errorHandler = new OAuthErrorHandler();
            throw errorHandler.fromMetadataError(e, this.configuration.getAuthority());
        }
    }
}
