//package com.bluebear.cinemax.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "SupportChat")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class SupportChat {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "ChatID")
//    private Integer id;
//
//    // Khách hàng bắt đầu cuộc trò chuyện
//    @ManyToOne
//    @JoinColumn(name = "CustomerID", nullable = false)
//    private Customer customer;
//
//    @ManyToOne
//    @JoinColumn(name = "EmployeeID")
//    private Employee employee;
//
//    @Column(name = "CreatedAt", nullable = false)
//    private LocalDateTime createdAt;
//
//    // Ví dụ: OPEN, PENDING, CLOSED
//    @Enumerated(EnumType.STRING)
//    @Column(name = "Status", length = 20)
//    private String status;
//
//    @OneToMany(mappedBy = "supportChat", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ChatMessage> messages;
//}