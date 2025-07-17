package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Theater_Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Theater")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID")
    private Integer theaterID;

    @Column(name = "TheaterName", nullable = false, length = 100)
    private String theaterName;

    @Column(name = "Address", nullable = false, length = 100)
    private String address;

    @Column(name = "Image", nullable = false, length = 255)
    private String image;

    @Column(name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @Column(name= "Longitude")
    private Double longitude;
    @Column(name= "Latitude")
    private Double latitude;

    @Column(name = "ServiceRate", nullable = true)
    private Double serviceRate;

    @Column(name = "Status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Theater_Status status;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TheaterStock> theaterStock;

    @OneToMany(mappedBy = "theater")
    private List<Employee> cashiers;

    @OneToMany(mappedBy = "theater")
    private List<Room> rooms;
}
