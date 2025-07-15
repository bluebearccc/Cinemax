//package com.bluebear.cinemax.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.List;
//
//@Entity
//@Table(name = "ScheduleChatRoom")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ScheduleChatRoom {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "RoomID")
//    private Integer id;
//
//    @OneToOne
//    @JoinColumn(name = "ScheduleID", nullable = false, unique = true)
//    private Schedule schedule;
//
//    @Column(name = "RoomName", length = 255)
//    private String roomName;
//
//    @OneToMany(mappedBy = "scheduleChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> messages;
//}