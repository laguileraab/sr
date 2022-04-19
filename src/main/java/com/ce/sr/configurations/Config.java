package com.ce.sr.configurations;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class Config implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (System.getenv("PORT") != null) {
            factory.setPort(Integer.valueOf(System.getenv("PORT")));
        }
        else{
            factory.setPort(80);
        }
        factory.setContextPath("");
    }
}