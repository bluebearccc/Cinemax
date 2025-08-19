package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActorDTO {
    private Integer actorID;
    private String actorName;
    private String image;
    private List<String> movies;
}