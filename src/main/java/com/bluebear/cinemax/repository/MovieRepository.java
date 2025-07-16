package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    // ==================== MOVIE QUERIES ====================

    // Tìm phim theo trạng thái
    List<Movie> findByStatus(Movie_Status status);

    // Tìm phim đang chiếu (trong khoảng thời gian hiện tại)
    @Query("SELECT m FROM Movie m " +
            "WHERE m.startDate <= :currentDate " +
            "AND m.endDate >= :currentDate " +
            "ORDER BY m.startDate ASC")
    List<Movie> findActiveMovies(@Param("currentDate") LocalDateTime currentDate);

    // Tìm phim sắp chiếu
    @Query("SELECT m FROM Movie m " +
            "WHERE m.startDate > :currentDate " +
            "ORDER BY m.startDate ASC")
    List<Movie> findUpcomingMovies(@Param("currentDate") LocalDateTime currentDate);

    // Tìm phim theo tên (có thể search) - không phân biệt hoa thường
    List<Movie> findByMovieNameContainingIgnoreCase(String movieName);

    // ==================== MOVIE-GENRE QUERIES (SIMPLIFIED) ====================

    // Tìm phim theo genre sử dụng @ManyToMany relationship
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreId")
    List<Movie> findMoviesByGenreIdJoin(@Param("genreId") Integer genreId);

    // ==================== MOVIE-ACTOR QUERIES (SIMPLIFIED) ====================

    // Tìm phim theo actor sử dụng @ManyToMany relationship
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE a.actorID = :actorId")
    List<Movie> findMoviesByActorIdJoin(@Param("actorId") Integer actorId);
}