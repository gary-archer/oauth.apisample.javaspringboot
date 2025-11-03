package com.authsamples.api.host.startup;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Http2Configuration {

    public Http2Configuration() {
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> getWebServerFactoryCustomizer() {

        return factory -> {
            var httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            httpConnector.setPort(8080);
            factory.addConnectorCustomizers(connector -> connector.addUpgradeProtocol(new Http2Protocol()));
            factory.addAdditionalTomcatConnectors(httpConnector);
        };
    }
}
