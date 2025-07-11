package com.bluebear.cinemax.service.chatservice;


import com.bluebear.cinemax.dto.ChatRequest;
import com.bluebear.cinemax.function.TestFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class ChatService {
    private final ChatClient chatClient;
    private final SimpleVectorStore simpleVectorStore;
    private final QuestionAnswerAdvisor qaAdvisor;
    private final TestFunction testFunction;

    @Autowired
    public ChatService(@Qualifier("groqChatClientBuilder") ChatClient.Builder builder,
                       SimpleVectorStore simpleVectorStore,
                       TestFunction testFunction) {
        this.simpleVectorStore = simpleVectorStore;
        this.testFunction = testFunction;

        this.chatClient = builder
                .build();

        this.qaAdvisor = QuestionAnswerAdvisor.builder(simpleVectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .similarityThreshold(0.8d)
                                .topK(6)
                                .build()
                )
                .build();

    }

    public String chat(ChatRequest request) {
        SystemMessage systemMessage = new SystemMessage("""
                You are Cinemax AI. 
                - When asked "who are you", answer clearly: "I am Cinemax AI, your movie assistant."
                
                Instructions:
                - Always respond with **short, clear, and direct answers**.
                - **Do not** start answers with phrases like: "According to the data", "Based on the document", or similar.
                - When using a tool, return **only the tool's result as your answer** — do not add extra context.
                - If a question cannot be answered using documents or tools, reply it yourself.
                """);

        UserMessage userMessage = new UserMessage(request.getMessage());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse response = chatClient.prompt(prompt)
                .advisors(qaAdvisor)
                .tools(testFunction)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }
}
