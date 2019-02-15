package com.mycompany.api.basicapi.framework.oauth;

import com.mycompany.api.basicapi.configuration.Configuration;
import com.mycompany.api.basicapi.framework.errors.ErrorHandler;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/*
 * A class to download Open Id Connect metadata at application startup
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class IssuerMetadata {

    /*
     * The application configuration
     */
    private final OauthConfiguration oauthConfiguration;

    /*
     * Metadata is downloaded at application startup
     */
    @Getter
    private OIDCProviderMetadata metadata;

    /*
     * Receive the configuration at startup
     */
    public IssuerMetadata(Configuration configuration) {

        this.oauthConfiguration = configuration.getOauth();
    }

    /*
     * Read the metadata here
     */
    @PostConstruct
    public void init(){

        try {
            // Create the issuer object
            var issuer = new Issuer(this.oauthConfiguration.getAuthority());

            // Make the HTTP request for metadata
            var request = new OIDCProviderConfigurationRequest(issuer).toHTTPRequest();
            HTTPResponse response = request.send();
            this.metadata = OIDCProviderMetadata.parse(response.getContentAsJSONObject());

        } catch(Exception e) {

            // Report errors
            throw ErrorHandler.fromMetadataError(e, this.oauthConfiguration.getAuthority());
        }
    }
}
