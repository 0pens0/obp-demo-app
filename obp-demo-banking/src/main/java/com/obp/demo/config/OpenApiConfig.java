package com.obp.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OBP Demo Banking API")
                        .version("1.0.0")
                        .description("API documentation for OBP Demo Banking Application. " +
                                "This application integrates with the Open Bank Project sandbox " +
                                "to provide banking services including account management, " +
                                "transaction history, and AI-powered chatbot.")
                        .contact(new Contact()
                                .name("OBP Demo Team")
                                .email("support@obpdemo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://your-production-url.com")
                                .description("Production server")
                ));
    }
}
