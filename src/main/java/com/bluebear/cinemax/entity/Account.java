package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccountID")
    private Integer id;

    @Column(unique = true, name = "Email")
    private String email;

    @Column(nullable = true, name = "[Password]")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "[Role]", nullable = false)
    private Role role;

    @Column(name = "Status")
    private Boolean status;

    public Account(String email, String password, Role role, Boolean status) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }
}
