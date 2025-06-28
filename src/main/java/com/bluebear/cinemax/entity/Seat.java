package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Seat")
@Getter
@Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer seatId;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;


    private String seatType; // 'Couple' or 'Single'

    private String position; // A1, A2...

    private boolean isVIP;

    private Double unitPrice;

    private String status; // 'Active', 'Inactive'


}
