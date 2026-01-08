package com.obp.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Configuration
 * 
 * This configuration supports Tanzu service binding for AI services.
 * When deployed to Tanzu, the service binding will automatically inject
 * the necessary credentials (e.g., OPENAI_API_KEY) which Spring Boot
 * will use to configure the OpenAiChatModel bean.
 */
@Configuration
public class SpringAiConfig {

    /**
     * Creates a ChatClient bean when OpenAiChatModel is available.
     * The OpenAiChatModel is auto-configured by Spring AI based on
     * spring.ai.openai.api-key property, which can be provided via:
     * - Environment variable: OPENAI_API_KEY
     * - Tanzu service binding: Automatically injected credentials
     * - application.yml: Direct configuration
     */
    @Bean
    @ConditionalOnBean(OpenAiChatModel.class)
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
