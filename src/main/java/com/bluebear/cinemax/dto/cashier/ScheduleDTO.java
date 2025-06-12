package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
    private Integer scheduleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private MovieDTO movie;
    private RoomDTO room;
    private String status;
}
