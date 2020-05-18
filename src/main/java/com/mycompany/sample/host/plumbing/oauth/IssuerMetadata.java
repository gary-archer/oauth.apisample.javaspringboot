package com.mycompany.sample.host.plumbing.oauth;

import com.mycompany.sample.host.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.host.plumbing.errors.ErrorUtils;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.Getter;

/*
 * A class to download Open Id Connect metadata at application startup
 */
public final class IssuerMetadata {

    private final OAuthConfiguration configuration;

    @Getter
    private OIDCProviderMetadata metadata;

    public IssuerMetadata(final OAuthConfiguration configuration) {

        this.configuration = configuration;
    }

    /*
     * Read the metadata here
     */
    public void initialize() {

        try {
            // Create the issuer object
            var issuer = new Issuer(this.configuration.getAuthority());

            // Make the HTTP request for metadata
            var request = new OIDCProviderConfigurationRequest(issuer).toHTTPRequest();
            HTTPResponse response = request.send();
            this.metadata = OIDCProviderMetadata.parse(response.getContentAsJSONObject());

        } catch (Throwable e) {

            // Throw an understandable error
            throw ErrorUtils.fromMetadataError(e, this.configuration.getAuthority());
        }
    }
}
