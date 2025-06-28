package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Schedule;
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
    private MovieDTO movie;
    private RoomDTO room;

    public ScheduleDTO(Schedule schedule) {
        this.scheduleID = schedule.getScheduleId();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.movieID = schedule.getMovie().getMovieID();
        this.roomID = schedule.getRoom().getRoomID();
        this.status = schedule.getStatus();


        if (schedule.getMovie() != null) {
            this.movie = new MovieDTO();
            this.movie.setMovieID(schedule.getMovie().getMovieID());
            this.movie.setMovieName(schedule.getMovie().getMovieName());
        }

        if (schedule.getRoom() != null) {
            this.room = new RoomDTO();
            this.room.setRoomID(schedule.getRoom().getRoomID());
            this.room.setName(schedule.getRoom().getName());
        }
    }

}
