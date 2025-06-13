package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "Movie_Genre", schema = "dbo")
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @Column(name = "GenreID", nullable = false)
    private int genreId;

    @Column(name = "MovieID", nullable = false)
    private int movieId;

    public MovieGenre() {
    }

    public MovieGenre(int id, int genreId, int movieId) {
        this.id = id;
        this.genreId = genreId;
        this.movieId = movieId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieGenre that = (MovieGenre) o;
        return id == that.id &&
                genreId == that.genreId &&
                movieId == that.movieId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, genreId, movieId);
    }

    @Override
    public String toString() {
        return "MovieGenre{" +
                "id=" + id +
                ", genreId=" + genreId +
                ", movieId=" + movieId +
                '}';
    }
}
