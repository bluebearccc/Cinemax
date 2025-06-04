package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.RoomType;
import com.bluebear.cinemax.enums.TheaterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Integer roomId;
    private Integer theaterId;
    private String name;
    private Integer column;
    private Integer row;
    private RoomType typeOfRoom;
    private TheaterStatus status;
    private TheaterDTO theater;
}