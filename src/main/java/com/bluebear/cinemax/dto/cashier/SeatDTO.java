package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Integer seatId;
    private RoomDTO room;
    private String seatType;
    private String position;
    private Boolean isVIP;
    private Double unitPrice;
    private String status;
    private Boolean isBooked = false;
    private String bookingStatus = "AVAILABLE"; // "AVAILABLE", "CONFIRMED", "PENDING", "CANCELLED"
    private Integer bookingId;

    public SeatDTO(String pos) {
        this.position = pos;
    }
}