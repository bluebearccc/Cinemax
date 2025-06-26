package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Room")
public class Room {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private int roomId;


    @ManyToOne
    @JoinColumn(name = "TheaterID", insertable = false, updatable = false)
    private Theater theater;

    @Column(name = "Name", length = 10, nullable = false)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private int column;

    @Column(name = "Row", nullable = false)
    private int row;

    @Column(name = "TypeOfRoom", length = 20)
    private String typeOfRoom;

    // Constructors



}
