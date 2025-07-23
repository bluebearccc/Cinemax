package com.bluebear.cinemax.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Value("${groq.api.key}")
    private String apiKey;

    @Bean
    @Qualifier("groqChatClientBuilder")
    public ChatClient.Builder groqChatClient() {
        String baseUrl = "https://api.groq.com/openai";
        String modelName = "llama3-70b-8192";

        var openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey).build();
        var options = OpenAiChatOptions.builder().model(modelName).build();
        var groqChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(options).build();
        return ChatClient.builder(groqChatModel);
    }

//    @Bean
//    @Qualifier("ollamaChatClientBuilder")
//    public ChatClient.Builder ollamaChatClientBuilder(OllamaChatModel ollamaChatModel) {
//        return ChatClient.builder(ollamaChatModel);
//    }

    @Bean
    @Qualifier("openAiChatClientBuilder")
    public ChatClient.Builder openAiChatClientBuilder(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel);
    }

}


