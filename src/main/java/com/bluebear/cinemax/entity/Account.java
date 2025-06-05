package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer id;

    @Column(name = "Email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "Password", nullable = false, length = 50)
    private String password;

    @Column(name = "Role", nullable = false, length = 50)
    private String role;

    @Column(name = "Status", nullable = false)
    private boolean status;
}
