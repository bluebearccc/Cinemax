package com.bluebear.cinemax.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailSeatDTO {
    private Integer id;
    private Integer invoiceID;
    private Integer seatID;
    private Integer scheduleID;
}