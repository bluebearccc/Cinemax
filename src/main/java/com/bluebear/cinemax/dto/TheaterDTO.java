package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.Theater_Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheaterDTO {
    private int id;
    private String name;
    private String address;
    private String image;
    private int roomQuantity;
    private Theater_Status status;
}
