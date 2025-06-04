package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enums.SeatType;
import com.bluebear.cinemax.enums.TheaterStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SeatID")
    private Integer seatId;

    @ManyToOne
    @JoinColumn(name = "RoomID", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "SeatType", length = 10)
    private SeatType seatType;

    @Column(name = "Position", nullable = false, length = 10)
    private String position;

    @Column(name = "IsVIP", nullable = false)
    private Boolean isVIP;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20)
    private TheaterStatus status;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
    private List<DetailSeat> detailSeats;
}

