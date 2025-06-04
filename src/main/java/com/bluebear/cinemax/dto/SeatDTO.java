package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.SeatType;
import com.bluebear.cinemax.enums.TheaterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Integer seatId;
    private Integer roomId;
    private SeatType seatType;
    private String position;
    private Boolean isVIP;
    private BigDecimal unitPrice;
    private TheaterStatus status;
    private RoomDTO room;
}