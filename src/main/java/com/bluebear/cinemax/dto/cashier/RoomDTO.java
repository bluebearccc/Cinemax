package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Integer roomId;
    private String name;
    private Integer column;
    private Integer row;
    private String typeOfRoom; // "Single" / "Couple"
    private String status;     // "Active" / "Inactive"
}
