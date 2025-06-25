package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Schedule_Status;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDTO {
    private Integer scheduleID;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer movieID;
    private Schedule_Status status;
    private Integer roomID;
    private String roomType;
    private String roomName;

    private SeatAvailabilityDTO seatAvailability;

    public String getFormattedMovieDate() {
        return startTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getFormattedStartTime() {
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedEndTime() {
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getShowDateKey() {
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}