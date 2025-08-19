package com.bluebear.cinemax.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String content;
    private Integer senderId;
    private String senderName;
}