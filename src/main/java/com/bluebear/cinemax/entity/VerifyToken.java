package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "VerifyToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, name = "Email")
    private String email;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, name = "[Password]")
    private String password;

    @Column(name = "FullName", nullable = false)
    private String fullName;

}

