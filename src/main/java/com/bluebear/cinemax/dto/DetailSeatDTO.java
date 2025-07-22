package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.DetailSeat_Status;
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
    private  DetailSeat_Status status;

}