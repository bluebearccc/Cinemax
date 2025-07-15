package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name = "Id")
    private Integer id;

    @Column(nullable = false, unique = true, name = "Email")
    private String email;

    @Column(nullable = false, unique = true, name = "Token")
    private String token;

    @Column(nullable = false, name = "ExpiresAt")
    private Date expiresAt;

    @Column(nullable = false, name = "[Password]")
    private String password;

    @Column(name = "FullName", nullable = false)
    private String fullName;

}

