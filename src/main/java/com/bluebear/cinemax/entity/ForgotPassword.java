package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;

}

