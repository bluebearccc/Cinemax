package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Movie_Genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GenreID", nullable = false)
    private Genre genre;

    // Constructor without ID for creation
    public MovieGenre(Movie movie, Genre genre) {
        this.movie = movie;
        this.genre = genre;
    }

    // Backward compatibility - maintain old method names
    public Integer getMovieGenreId() {
        return id;
    }

    public void setMovieGenreId(Integer movieGenreId) {
        this.id = movieGenreId;
    }

    // Override equals and hashCode based on MovieID and GenreID for entity comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieGenre)) return false;

        MovieGenre that = (MovieGenre) o;

        if (movie != null ? !movie.getMovieId().equals(that.movie != null ? that.movie.getMovieId() : null) : that.movie != null)
            return false;
        return genre != null ? genre.getGenreId().equals(that.genre != null ? that.genre.getGenreId() : null) : that.genre == null;
    }

    @Override
    public int hashCode() {
        int result = movie != null ? movie.getMovieId().hashCode() : 0;
        result = 31 * result + (genre != null ? genre.getGenreId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MovieGenre{" +
                "id=" + id +
                ", movieId=" + (movie != null ? movie.getMovieId() : null) +
                ", genreId=" + (genre != null ? genre.getGenreId() : null) +
                '}';
    }
}