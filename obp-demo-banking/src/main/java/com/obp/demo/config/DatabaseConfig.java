package com.obp.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.obp.demo.repository")
public class DatabaseConfig {
    // Configuration for JPA repositories
    // PostgreSQL and vector store configuration is handled via application.yml
}
