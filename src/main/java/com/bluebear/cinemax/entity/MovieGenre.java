// MovieGenre.java - Fixed Entity class
package com.bluebear.cinemax.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Movie_Genre") // Đảm bảo tên bảng chính xác
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id") // Chính xác theo database
    private Integer id; // Đổi tên field cho rõ ràng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GenreID", nullable = false)
    private Genre genre;

    // Constructors
    public MovieGenre() {}

    public MovieGenre(Movie movie, Genre genre) {
        this.movie = movie;
        this.genre = genre;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Backward compatibility - giữ method cũ
    public Integer getMovieGenreId() {
        return id;
    }

    public void setMovieGenreId(Integer movieGenreId) {
        this.id = movieGenreId;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    // Equals and HashCode (dựa trên MovieID và GenreID)
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