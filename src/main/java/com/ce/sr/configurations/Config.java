package com.ce.sr.configurations;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

public class Config implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (System.getenv("PORT") != null) {
            factory.setPort(Integer.valueOf(System.getenv("PORT")));
        }
        factory.setContextPath("");
    }
}