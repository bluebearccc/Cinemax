package com.bluebear.cinemax.entity;

import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SeatID")
    private Integer seatID;

    @ManyToOne()
    @JoinColumn(name = "RoomID", nullable = false)
    private Room room;

    @Column(name = "SeatType", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TypeOfSeat seatType;

    @Column(name = "Position", nullable = false, length = 10)
    private String position;

    @Column(name = "IsVIP", nullable = false)
    private Boolean isVIP;

    @Column(name = "UnitPrice", nullable = false)
    private Double unitPrice;

    @Column(name = "Status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Seat_Status status;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSeat> detailSeatList;
}
