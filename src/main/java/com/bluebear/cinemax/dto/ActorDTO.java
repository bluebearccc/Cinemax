package com.bluebear.cinemax.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorDTO {

    private Integer actorID;
    private String actorName;
    private String image;
}