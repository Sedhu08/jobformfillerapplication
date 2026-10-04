package com.jobfiller.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /**
     * Exposes a ChatClient bean backed by Ollama.
     * The ChatClient.Builder is auto-configured by spring-ai-ollama-spring-boot-starter.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
