package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.ServiceFeedbackStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ServiceFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @Column(name = "Content", length = 100)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 10)
    private ServiceFeedbackStatus status;
}
