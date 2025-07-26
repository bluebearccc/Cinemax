package com.bluebear.cinemax.service.chatservice;


import com.bluebear.cinemax.dto.ChatRequest;
import com.bluebear.cinemax.function.CommonFunction;
import com.bluebear.cinemax.function.MovieFunction;
import com.bluebear.cinemax.function.ScheduleFunction;
import com.bluebear.cinemax.function.TheaterFunction;
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
    private final QuestionAnswerAdvisor qaAdvisor;

    @Autowired
    public ChatService(@Qualifier("openAiChatClientBuilder") ChatClient.Builder builder,
                       SimpleVectorStore simpleVectorStore,
                       CommonFunction commonFunction, MovieFunction movieFunction, ScheduleFunction scheduleFunction, TheaterFunction theaterFunction) {
        this.chatClient = builder.defaultTools(commonFunction, movieFunction, scheduleFunction, theaterFunction).build();

        this.qaAdvisor = QuestionAnswerAdvisor.builder(simpleVectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .similarityThreshold(0.8d)
                                .topK(6)
                                .build()
                ).build();
    }

    public String chat(ChatRequest request) {
        SystemMessage systemMessage = new SystemMessage("""
                You are Cinemax AI.
                Instructions:
                - Always respond with **short, clear, and direct answers**.
                - **Do not** start answers with phrases like: "According to the data", "Based on the document", or similar.
                - When calling the tool, calculate dayOffset as the number of days from today (e.g., 0 = today, 1 = tomorrow).
                - When using a tool, return **only the tool's result as your answer** with format 
                If any required parameter is missing or empty (e.g., empty string, null, undefined),
                Instead, politely ask the user for the missing information before proceeding.
                Only proceed with the tool call once you have all necessary information.
                - When the answer contains a list, format it using list format like:
                - item 1
                - item 2
                - item 3
                - Respond naturally and conversationally like a human. 
                - Do not mention or reference any tools, APIs, documents, models, or systems you may be using. 
                - If a question cannot be answered using documents or tools, reply it yourself but do not answer by saying that you don’t know or that the tools failed. 
                - Always give a helpful, polite response based on your own understanding. If a movie or actor is not found, simply say so, for example: 'Tom Hardy has no movies listed.
                """);

        UserMessage userMessage = new UserMessage(request.getMessage());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        ChatResponse response = chatClient.prompt(prompt)
                .advisors(qaAdvisor)
                .call()
                .chatResponse();
        return response.getResult().getOutput().getText();
    }
}
