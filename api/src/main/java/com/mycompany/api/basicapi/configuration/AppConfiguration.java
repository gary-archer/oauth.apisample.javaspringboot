package com.mycompany.api.basicapi.configuration;

import lombok.Getter;
import lombok.Setter;

/*
 * Application specific properties
 */
public class AppConfiguration {

    @Getter
    @Setter
    private String[] trustedOrigins;

    @Getter
    @Setter
    public String sslCertificateFileName;

    @Getter
    @Setter
    public String sslCertificatePassword;

    @Getter
    @Setter
    public String proxyUrl;

    @Getter
    @Setter
    public boolean useProxy;
}
