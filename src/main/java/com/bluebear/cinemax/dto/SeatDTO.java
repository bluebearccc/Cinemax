package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Integer seatID;
    private Integer roomID;
    private TypeOfSeat seatType;
    private String position;
    private String name; // Thêm dòng này
    private Boolean isVIP;
    private Double unitPrice;
    private Seat_Status status;
}