package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailSeatDTO {
    private Integer id;
    private Integer invoiceId;
    private Integer seatId;
    private SeatStatus status;
    private Integer scheduleId;
    private SeatDTO seat;
    private ScheduleDTO schedule;
}