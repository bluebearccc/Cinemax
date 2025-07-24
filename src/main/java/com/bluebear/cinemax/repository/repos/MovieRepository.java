package com.bluebear.cinemax.repository.repos;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository("movieAdminrepo")
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

    // ==================== SCHEDULE VALIDATION QUERIES ====================

    // Kiểm tra xem movie có schedule với DetailSeat trạng thái "Booked" không
    @Query("SELECT COUNT(ds) > 0 FROM Schedule s " +
            "JOIN s.detailSeatList ds " +
            "WHERE s.movie.movieID = :movieId " +
            "AND ds.status = com.bluebear.cinemax.enumtype.DetailSeat_Status.Booked")
    boolean existsInScheduleWithBookedSeats(@Param("movieId") Integer movieId);

    // Kiểm tra xem movie có schedule đang hoạt động không
    @Query("SELECT COUNT(s) > 0 FROM Schedule s " +
            "WHERE s.movie.movieID = :movieId " +
            "AND s.status = com.bluebear.cinemax.enumtype.Schedule_Status.Active")
    boolean hasActiveSchedule(@Param("movieId") Integer movieId);

    // Lấy số lượng schedule của movie
    @Query("SELECT COUNT(s) FROM Schedule s WHERE s.movie.movieID = :movieId")
    long countSchedulesByMovieId(@Param("movieId") Integer movieId);

    // Kiểm tra xem movie có tồn tại trong schedule không (giữ lại method cũ nếu cần)
    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.movie.movieID = :movieId")
    boolean existsInSchedule(@Param("movieId") Integer movieId);

    // ==================== MOVIE-GENRE QUERIES (SIMPLIFIED) ====================

    // Tìm phim theo genre sử dụng @ManyToMany relationship
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreID")
    List<Movie> findMoviesByGenreIDJoin(@Param("genreID") Integer genreID);

    // ==================== MOVIE-ACTOR QUERIES (SIMPLIFIED) ====================

    // Tìm phim theo actor sử dụng @ManyToMany relationship
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE a.actorID = :actorId")
    List<Movie> findMoviesByActorIdJoin(@Param("actorId") Integer actorId);
}