package com.bluebear.cinemax.entity;

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
