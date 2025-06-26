package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Seat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SeatDTO {
    private Integer seatId;
    private String position;
    private String seatType;
    private boolean VIP;
    private BigDecimal unitPrice;
    private boolean isBooked;
}
