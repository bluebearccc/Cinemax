package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.Theater_Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Theater")
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID")
    private Integer theaterId;

    @Column(name = "TheaterName", length = 100, nullable = false)
    private String theaterName;

    @Column(name = "Address", length = 100, nullable = false)
    private String address;

    @Column(name = "Image", length = 255, nullable = false)
    private String image;

    @Column(name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 50, nullable = false)
    private Theater_Status  status;

    @OneToMany(mappedBy = "theater")
    private Set<TheaterStock> theaterStock;

    @OneToMany(mappedBy = "theater")
    private Set<Employee> employees;
}
