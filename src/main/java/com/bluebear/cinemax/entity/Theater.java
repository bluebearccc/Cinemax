package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Theater_Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
@Builder
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
    private Integer theaterID;

    @Column(name = "TheaterName", length = 100, nullable = false)
    private String theaterName;

    @Column(name = "Address", length = 100, nullable = false)
    private String address;

    @Column(name = "Image", length = 255, nullable = false)
    private String image;

    @Column(name = "RoomQuantity", nullable = false)
    private Integer roomQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private Theater_Status  status;

    @OneToMany(mappedBy = "theater")
    private List<TheaterStock> theaterStock;

    @OneToMany(mappedBy = "theater")
    private List<Employee> cashiers;

    @OneToMany(mappedBy = "theater")
    private List<Room> rooms;
}
