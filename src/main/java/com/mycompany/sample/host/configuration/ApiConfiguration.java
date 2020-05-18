package com.mycompany.sample.host.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/*
 * API specific properties
 */
public class ApiConfiguration {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String sslCertificateFileName;

    @Getter
    @Setter
    private String sslCertificatePassword;

    @Getter
    @Setter
    private String[] trustedOrigins;

    @Getter
    @Setter
    private String proxyUrl;

    @Getter
    @Setter
    private boolean useProxy;
}
