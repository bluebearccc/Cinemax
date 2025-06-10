package com.bluebear.cinemax.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TheaterID", nullable = false)
    private Theater theater;

    @Column(name = "Name", nullable = false, length = 10)
    private String name;

    @Column(name = "Collumn", nullable = false)
    private Integer column;

    @Column(name = "Row", nullable = false)
    private Integer row;

    @Enumerated(EnumType.STRING)
    @Column(name = "TypeOfRoom", nullable = false)
    private RoomType typeOfRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules;

    public enum RoomType {
        Couple, Single
    }

    public enum RoomStatus {
        Active, Inactive
    }
}