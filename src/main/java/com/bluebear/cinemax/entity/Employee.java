package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.Employee_Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmployeeID")
    private Integer employeeId;

    @Column(name = "Position", length = 255, nullable = false)
    private String position;

    @Column(name = "Status", length = 50, nullable = false)
    private Employee_Status status;

    @OneToOne
    @JoinColumn(name = "AccountID", nullable = false)
    private Account account;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "TheaterID", nullable = false)
//    private Theater theater;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID" , nullable = false, referencedColumnName = "TheaterID")
    private Theater theater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AdminID")
    private Employee admin;

    @Nationalized
    @Column(name = "FullName", length = 100, nullable = false)
    private String fullName;

}