package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Schedule_Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDTO {
    private Integer scheduleID;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer movieID;
    private Integer roomID;
    private Integer numberOfSeatsRemain;
    private Schedule_Status status;
}
