package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Theater")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID")
    private Integer theaterID;

    @Column(name = "TheaterName")
    private String theaterName;

    @Column(name = "Address")
    private String theaterAddress;

    @Column(name = "Image")
    private String image;

    @Column(name = "RoomQuantity")
    private Integer roomQuantity;

    @OneToOne
    @JoinColumn(name = "AdminID")
    private Employee admin;
}
