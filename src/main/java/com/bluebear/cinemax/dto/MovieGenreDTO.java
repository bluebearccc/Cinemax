package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenreDTO {
    private Integer id;
    private Integer movieId;
    private String movieName;
    private Integer genreId;
    private String genreName;

    // Constructor without ID for creation
    public MovieGenreDTO(Integer movieId, String movieName, Integer genreId, String genreName) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.genreId = genreId;
        this.genreName = genreName;
    }

    // Backward compatibility - maintain old method names
    public Integer getMovieGenreId() {
        return id;
    }

    public void setMovieGenreId(Integer movieGenreId) {
        this.id = movieGenreId;
    }

    // Helper methods for validation and display
    public boolean isValid() {
        return movieId != null && genreId != null;
    }

    public String getDisplayInfo() {
        return (movieName != null ? movieName : "Unknown Movie") +
                " - " +
                (genreName != null ? genreName : "Unknown Genre");
    }

    // Override equals and hashCode based on movieId and genreId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieGenreDTO)) return false;

        MovieGenreDTO that = (MovieGenreDTO) o;

        if (movieId != null ? !movieId.equals(that.movieId) : that.movieId != null) return false;
        return genreId != null ? genreId.equals(that.genreId) : that.genreId == null;
    }

    @Override
    public int hashCode() {
        int result = movieId != null ? movieId.hashCode() : 0;
        result = 31 * result + (genreId != null ? genreId.hashCode() : 0);
        return result;
    }
}