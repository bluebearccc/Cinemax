package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Room_Status;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfRoom;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private Integer roomID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "Name", nullable = false, length = 10)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private int collumn;

    @Column(name = "Row", nullable = false)
    private int row;

    @Column(name = "TypeOfRoom", nullable = false, length = 20)
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
