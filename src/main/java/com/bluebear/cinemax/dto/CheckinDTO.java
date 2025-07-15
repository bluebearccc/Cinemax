package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckinDTO {
    private String status;
    private String message;
    private BookingResultDTO bookingDetails;

}
