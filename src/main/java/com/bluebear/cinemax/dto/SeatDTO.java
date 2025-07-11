package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Integer seatID;
    private Integer roomID;
    private TypeOfSeat seatType;
    private String position;
    private Boolean isVIP;
    private Double unitPrice;
    private boolean isBooked;
    private Seat_Status status;
}
