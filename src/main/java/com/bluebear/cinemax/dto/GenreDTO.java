package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDTO {
    private Integer genreId;
    private String genreName;

    // Helper methods for validation and display
    public boolean isValid() {
        return genreName != null && !genreName.trim().isEmpty();
    }

    public String getDisplayName() {
        return genreName != null ? genreName.trim() : "";
    }
}