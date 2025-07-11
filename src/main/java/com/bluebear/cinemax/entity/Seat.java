package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SeatID")
    private Integer seatID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "SeatType", nullable = false, length = 10)
    private TypeOfSeat seatType;

    @Column(name = "Position", nullable = false, length = 10)
    private String position;

    @Column(name = "IsVIP", nullable = false)
    private boolean isVIP;

    @Column(name = "UnitPrice", nullable = false)
    private Double unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private Seat_Status status; // 'Active', 'Inactive'


}
