package com.bluebear.cinemax.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoomID", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "SeatType", nullable = false)
    private SeatType seatType;

    @Column(name = "Position", nullable = false, length = 10)
    private String position;

    @Column(name = "IsVIP", nullable = false)
    private Boolean isVIP;

    @Column(name = "UnitPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private SeatStatus status;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DetailSeat> detailSeats;

    public enum SeatType {
        Couple, Single
    }

    public enum SeatStatus {
        Active, Inactive
    }
}