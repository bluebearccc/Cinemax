package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Employee_Status;
import com.bluebear.cinemax.enumtype.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "EmployeeID")
    private Integer id;

    @Column(name = "Position")
    private Role position;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private Employee_Status status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "AccountID")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @ManyToOne
    @JoinColumn(name = "AdminID")
    private Employee admin;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    public Employee(Role position, Employee_Status status, Account account, Theater theater, Employee admin, String fullName) {
        this.position = position;
        this.status = status;
        this.account = account;
        this.theater = theater;
        this.admin = admin;
        this.fullName = fullName;
    }
}
