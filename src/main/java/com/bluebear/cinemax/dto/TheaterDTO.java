package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.TheaterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDTO {
    private Integer theaterId;
    private String theaterName;
    private String address;
    private String image;
    private Integer roomQuantity;
    private TheaterStatus status;
}