package com.obp.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "obp.api")
@Getter
@Setter
public class ObpApiConfig {
    private String baseUrl = "https://apisandbox.openbankproject.com";
    private String apiKey;
    private String version = "v5.1.0";
}
