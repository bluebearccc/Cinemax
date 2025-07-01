package com.bluebear.cinemax.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieGenreDTO {
    private Integer genreID;
    private String genreName;
}
