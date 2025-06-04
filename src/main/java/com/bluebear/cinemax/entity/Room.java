package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.RoomType;
import com.bluebear.cinemax.enums.TheaterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Room")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoomID")
    private Integer roomId;

    @ManyToOne
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "Name", nullable = false, length = 10)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private Integer column;

    @Column(name = "Row", nullable = false)
    private Integer row;

    @Enumerated(EnumType.STRING)
    @Column(name = "TypeOfRoom", length = 20)
    private RoomType typeOfRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private TheaterStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Schedule> schedules;
}
