package com.mycompany.sample.host.configuration;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * API specific properties
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class ApiConfiguration {

    @Getter
    @Setter
    private int _port;

    @Getter
    @Setter
    private String _sslCertificateFileName;

    @Getter
    @Setter
    private String _sslCertificatePassword;

    @Getter
    @Setter
    private boolean _useProxy;

    @Getter
    @Setter
    private String _proxyUrl;

    @Getter
    @Setter
    private String[] _webTrustedOrigins;
}
