package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Employee_Status;
import com.bluebear.cinemax.enumtype.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmployeeID")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Position", nullable = false, length = 50)
    private Role position;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private Employee_Status status;

    @OneToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @ManyToOne
    @JoinColumn(name = "AdminID")
    private Employee admin;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Invoice> invoiceList;
}
