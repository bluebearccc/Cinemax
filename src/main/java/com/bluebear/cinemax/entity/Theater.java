package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TheaterID")
    private Integer id;

    @Column(name = "TheaterName", nullable = false, length = 100)
    private String theaterName;

    @Column(name = "Address", nullable = false, length = 100)
    private String address;

    @Column(name = "Image", nullable = false, length = 255)
    private String image;

    @Column (name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Employee> employees;

    public Theater(String theaterName, String address, String image, Integer roomQuantity) {
        this.theaterName = theaterName;
        this.address = address;
        this.image = image;
        this.roomQuantity = roomQuantity;
    }


}
