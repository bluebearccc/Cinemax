package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer id;

    @Column(unique = true, name = "Email")
    private String email;

    @Column(nullable = true, name = "Password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private Role role;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private Account_Status status;
}
