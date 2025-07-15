//package com.bluebear.cinemax.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "ChatMessage")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ChatMessage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "MessageID")
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "SupportChatID")
//    private SupportChat supportChat;
//
//    @ManyToOne
//    @JoinColumn(name = "ScheduleChatRoomID")
//    private ScheduleChatRoom scheduleChatRoom;
//
//    @ManyToOne
//    @JoinColumn(name = "SenderID", nullable = false)
//    private Account sender;
//
//    @Column(name = "Content", nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @Column(name = "Timestamp", nullable = false)
//    private LocalDateTime timestamp;
//}