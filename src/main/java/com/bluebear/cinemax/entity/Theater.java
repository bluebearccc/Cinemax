package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Theater")
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID", nullable = false)
    private Integer theaterId;

    @Column(name = "TheaterName", nullable = false, length = 100)
    private String theaterName;

    @Column(name = "Address", nullable = false, length = 100)
    private String address;

    @Column(name = "Image", nullable = false, length = 255)
    private String image;

    @Column(name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @Column(name = "ServiceRate")
    private Integer serviceRate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;
}
