package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Schedule_Status;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDTO {
    private Integer scheduleID;
    private Date startTime;
    private Date endTime;
    private Integer movieID;
    private Integer roomID;
    private Schedule_Status status;
}
