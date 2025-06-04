package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.TheaterStatus;
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
    private Integer movieId;
    private Integer roomId;
    private TheaterStatus status;
    private MovieDTO movie;
    private RoomDTO room;
}
