package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    // Tìm phim theo trạng thái
    List<Movie> findByStatus(Movie.MovieStatus status);

    // Tìm phim đang chiếu (Active và trong khoảng thời gian)
    @Query("SELECT m FROM Movie m WHERE m.status = 'Active' AND m.startDate <= :currentDate AND m.endDate >= :currentDate")
    List<Movie> findActiveMovies(@Param("currentDate") LocalDate currentDate);

    // Tìm phim sắp chiếu
    @Query("SELECT m FROM Movie m WHERE m.status = 'Active' AND m.startDate > :currentDate")
    List<Movie> findUpcomingMovies(@Param("currentDate") LocalDate currentDate);

    // Tìm phim theo tên (có thể search) - không phân biệt hoa thường
    List<Movie> findByMovieNameContainingIgnoreCase(String movieName);

    // Tìm phim theo tên và trạng thái
    List<Movie> findByMovieNameContainingIgnoreCaseAndStatus(String movieName, Movie.MovieStatus status);

    // Tìm phim theo thể loại
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.genreId = :genreId AND m.status = 'Active'")
    List<Movie> findByGenreId(@Param("genreId") Integer genreId);

    // Tìm phim theo thể loại (bao gồm cả Removed)
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.genreId = :genreId")
    List<Movie> findByGenreIdIncludingRemoved(@Param("genreId") Integer genreId);

    // Tìm phim theo thể loại và trạng thái
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.genreId = :genreId AND m.status = :status")
    List<Movie> findByGenreIdAndStatus(@Param("genreId") Integer genreId, @Param("status") Movie.MovieStatus status);

    // Tìm phim theo diễn viên
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieActors ma WHERE ma.actor.actorId = :actorId AND m.status = 'Active'")
    List<Movie> findByActorId(@Param("actorId") Integer actorId);

    // Tìm phim theo diễn viên (bao gồm cả Removed)
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieActors ma WHERE ma.actor.actorId = :actorId")
    List<Movie> findByActorIdIncludingRemoved(@Param("actorId") Integer actorId);

    // Tìm top phim theo rating
    List<Movie> findTop10ByStatusOrderByMovieRateDesc(Movie.MovieStatus status);

    // Tìm phim theo nhiều criteria
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.movieGenres mg " +
            "WHERE (:movieName IS NULL OR LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%'))) " +
            "AND (:genreId IS NULL OR mg.genre.genreId = :genreId) " +
            "AND (:status IS NULL OR m.status = :status)")
    List<Movie> findMoviesByCriteria(@Param("movieName") String movieName,
                                     @Param("genreId") Integer genreId,
                                     @Param("status") Movie.MovieStatus status);

    // Tìm phim theo khoảng thời gian
    @Query("SELECT m FROM Movie m WHERE m.startDate >= :startDate AND m.endDate <= :endDate AND m.status = 'Active'")
    List<Movie> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Tìm phim theo rating tối thiểu
    @Query("SELECT m FROM Movie m WHERE m.movieRate >= :minRating AND m.status = 'Active' ORDER BY m.movieRate DESC")
    List<Movie> findByMinRating(@Param("minRating") Double minRating);

    // Tìm phim theo duration
    @Query("SELECT m FROM Movie m WHERE m.duration >= :minDuration AND m.duration <= :maxDuration AND m.status = 'Active'")
    List<Movie> findByDurationRange(@Param("minDuration") Integer minDuration, @Param("maxDuration") Integer maxDuration);

    // Tìm phim mới nhất
    @Query("SELECT m FROM Movie m WHERE m.status = 'Active' ORDER BY m.startDate DESC")
    List<Movie> findLatestMovies();

    // Tìm phim phổ biến (theo rating)
    @Query("SELECT m FROM Movie m WHERE m.status = 'Active' AND m.movieRate IS NOT NULL ORDER BY m.movieRate DESC")
    List<Movie> findPopularMovies();

    // Đếm phim theo trạng thái
    long countByStatus(Movie.MovieStatus status);

    // Đếm phim theo thể loại
    @Query("SELECT COUNT(DISTINCT m) FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.genreId = :genreId AND m.status = 'Active'")
    long countByGenreId(@Param("genreId") Integer genreId);

    // Kiểm tra phim có tồn tại theo tên
    boolean existsByMovieNameIgnoreCase(String movieName);

    // Tìm phim theo studio
    List<Movie> findByStudioContainingIgnoreCaseAndStatus(String studio, Movie.MovieStatus status);
}