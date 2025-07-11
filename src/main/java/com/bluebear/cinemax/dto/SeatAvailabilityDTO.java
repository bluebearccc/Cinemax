package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatAvailabilityDTO {

    private int totalSeats;
    private int availableSeats;

    private int totalVipSeats;
    private int availableVipSeats;

    private int totalRegularSeats;
    private int availableRegularSeats;

}