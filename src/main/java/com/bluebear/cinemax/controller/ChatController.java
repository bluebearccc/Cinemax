package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.ChatRequest;
import com.bluebear.cinemax.service.chatservice.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    ChatService chatService;

    @PostMapping("/ask")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

}
