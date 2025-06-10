package com.bluebear.cinemax.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @Column(name = "Content", length = 100)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private ServiceStatus status;

    public enum ServiceStatus {
        Suported, Not_Suported
    }
}