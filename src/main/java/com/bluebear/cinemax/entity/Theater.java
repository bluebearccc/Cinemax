package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.TheaterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Theater")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID")
    private Integer theaterId;

    @Column(name = "TheaterName", nullable = false, length = 100)
    private String theaterName;

    @Column(name = "Address", nullable = false, length = 100)
    private String address;

    @Column(name = "Image", nullable = false)
    private String image;

    @Column(name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private TheaterStatus status;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Room> rooms;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Employee> employees;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<TheaterStock> theaterStocks;
}
