package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieActorDTO {
    private Integer id;
    private Integer actorId;
    private String actorName;
    private Integer movieId;
    private String movieName;

    // Constructor without ID for creation
    public MovieActorDTO(Integer actorId, String actorName, Integer movieId, String movieName) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.movieId = movieId;
        this.movieName = movieName;
    }

    // Helper methods for validation and display
    public boolean isValid() {
        return actorId != null && movieId != null;
    }

    public String getDisplayInfo() {
        return (actorName != null ? actorName : "Unknown Actor") +
                " in " +
                (movieName != null ? movieName : "Unknown Movie");
    }
}