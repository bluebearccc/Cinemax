package com.bluebear.cinemax.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    @Qualifier("groqChatClientBuilder")
    public ChatClient.Builder groqChatClientBuilder(OpenAiChatModel groqChatModel) {
        return ChatClient.builder(groqChatModel);
    }

    @Bean
    @Qualifier("ollamaChatClientBuilder")
    public ChatClient.Builder ollamaChatClientBuilder(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel);
    }
}


