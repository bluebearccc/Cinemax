package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Room_Status;
import com.bluebear.cinemax.enumtype.TypeOfRoom;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    private Integer roomID;
    private Integer theaterID;
    private String name;
    private Integer column;
    private Integer row;
    private TypeOfRoom typeOfRoom;
    private Room_Status status;

    private List<SeatDTO> seats;
    private List<ScheduleDTO> schedules;
}
