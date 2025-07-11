package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.ServiceFeedback_Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ServiceFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "ServiceRate", nullable = false)
    private Integer serviceRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ServiceFeedback_Status status;
}

