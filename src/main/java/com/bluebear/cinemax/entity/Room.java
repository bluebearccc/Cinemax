package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "Name", length = 10, nullable = false)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private Integer column;
    
    @Column(name = "Row", nullable = false)
    private Integer row;

    @Column(name = "TypeOfRoom", length = 20, nullable = false)
    private String typeOfRoom;

    @OneToMany(mappedBy = "room")
    private Set<Schedule> schedule;
}