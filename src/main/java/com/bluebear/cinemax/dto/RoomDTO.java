package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Room;
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
    private Integer collumn;
    private Integer row;
    private TypeOfRoom typeOfRoom;
    private Room_Status status;

    private List<SeatDTO> seats;
    private List<ScheduleDTO> schedules;

    public RoomDTO(Room room) {
        this.roomID = room.getRoomID();
        this.theaterID=room.getTheater().getTheaterId();
        this.name=room.getName();
        this.collumn=room.getCollumn();
        this.row=room.getRow();
        this.typeOfRoom=room.getTypeOfRoom();
        this.status=room.getStatus();
    }
}
