package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Room_Status;
import com.bluebear.cinemax.enumtype.TypeOfRoom;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private Integer roomID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "Name", length = 10, nullable = false)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private Integer column;
    
    @Column(name = "Row", nullable = false)
    private Integer row;

    @Column(name = "TypeOfRoom", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeOfRoom typeOfRoom;

    @Column(name = "Status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Room_Status status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;
}