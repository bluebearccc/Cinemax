package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "ForgotPassword")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private Integer otp;

    @OneToOne
    @JoinColumn(name = "Accountid", nullable = false)
    private Account account;

    @Column(name = "expire_date", nullable = false)
    private LocalDateTime expiryDate;

}

