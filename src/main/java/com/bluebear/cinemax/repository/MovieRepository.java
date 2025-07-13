package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    /**
     * Tìm phim theo trạng thái
     */
    List<Movie> findByStatus(Movie_Status status);

    /**
     * Tìm phim đang chiếu (Active và trong khoảng thời gian)
     * FIXED: Sử dụng LocalDateTime comparison thay vì DATE() function
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.startDate <= CURRENT_TIMESTAMP " +
            "AND m.endDate >= CURRENT_TIMESTAMP " +
            "ORDER BY m.startDate ASC")
    List<Movie> findActiveMovies(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm phim sắp chiếu
     * FIXED: Sử dụng LocalDateTime comparison thay vì DATE() function
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.startDate > CURRENT_TIMESTAMP " +
            "ORDER BY m.startDate ASC")
    List<Movie> findUpcomingMovies(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm phim theo tên (có thể search) - không phân biệt hoa thường
     */
    List<Movie> findByMovieNameContainingIgnoreCase(String movieName);

    /**
     * Tìm phim theo tên và trạng thái
     */
    List<Movie> findByMovieNameContainingIgnoreCaseAndStatus(String movieName, Movie_Status status);

    /**
     * Tìm phim theo thể loại (chỉ Active)
     * FIXED: Sử dụng @ManyToMany relationship thay vì JOIN table
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreId " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.movieName ASC")
    List<Movie> findByGenreId(@Param("genreId") Integer genreId);

    /**
     * Tìm phim theo thể loại (bao gồm cả Removed)
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreId " +
            "ORDER BY m.movieName ASC")
    List<Movie> findByGenreIdIncludingRemoved(@Param("genreId") Integer genreId);

    /**
     * Tìm phim theo thể loại và trạng thái
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreId " +
            "AND m.status = :status " +
            "ORDER BY m.movieName ASC")
    List<Movie> findByGenreIdAndStatus(@Param("genreId") Integer genreId,
                                       @Param("status") Movie_Status status);

    /**
     * Tìm phim theo diễn viên (chỉ Active)
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE a.actorID = :actorId " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.startDate DESC")
    List<Movie> findByActorId(@Param("actorId") Integer actorId);

    /**
     * Tìm phim theo diễn viên (bao gồm cả Removed)
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE a.actorID = :actorId " +
            "ORDER BY m.startDate DESC")
    List<Movie> findByActorIdIncludingRemoved(@Param("actorId") Integer actorId);

    /**
     * Tìm top phim theo rating
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = :status " +
            "AND m.movieRate IS NOT NULL " +
            "ORDER BY m.movieRate DESC " +
            "LIMIT 10")
    List<Movie> findTop10ByStatusOrderByMovieRateDesc(@Param("status") Movie_Status status);

    /**
     * Tìm phim theo nhiều criteria
     * FIXED: Sử dụng LEFT JOIN với @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "WHERE (:movieName IS NULL OR UPPER(m.movieName) LIKE UPPER(CONCAT('%', :movieName, '%'))) " +
            "AND (:genreId IS NULL OR g.genreID = :genreId) " +
            "AND (:status IS NULL OR m.status = :status) " +
            "ORDER BY m.movieName ASC")
    List<Movie> findMoviesByCriteria(@Param("movieName") String movieName,
                                     @Param("genreId") Integer genreId,
                                     @Param("status") Movie_Status status);

    /**
     * Tìm phim theo khoảng thời gian
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.startDate >= :startDate " +
            "AND m.endDate <= :endDate " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.startDate ASC")
    List<Movie> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm phim theo khoảng thời gian (LocalDate version)
     * FIXED: Sử dụng LocalDateTime comparison
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.startDate >= :startDate " +
            "AND m.endDate <= :endDate " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.startDate ASC")
    List<Movie> findByDateRangeLocalDate(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm phim theo rating tối thiểu
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.movieRate >= :minRating " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.movieRate DESC")
    List<Movie> findByMinRating(@Param("minRating") Double minRating);

    /**
     * Tìm phim theo duration
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.duration >= :minDuration " +
            "AND m.duration <= :maxDuration " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.duration ASC")
    List<Movie> findByDurationRange(@Param("minDuration") Integer minDuration,
                                    @Param("maxDuration") Integer maxDuration);

    /**
     * Tìm phim mới nhất
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.startDate DESC")
    List<Movie> findLatestMovies();

    /**
     * Tìm phim phổ biến (theo rating)
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.movieRate IS NOT NULL " +
            "ORDER BY m.movieRate DESC")
    List<Movie> findPopularMovies();

    /**
     * Đếm phim theo trạng thái
     */
    long countByStatus(Movie_Status status);

    /**
     * Đếm phim theo thể loại
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT COUNT(DISTINCT m) FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID = :genreId " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active")
    long countByGenreId(@Param("genreId") Integer genreId);

    /**
     * Kiểm tra phim có tồn tại theo tên
     */
    boolean existsByMovieNameIgnoreCase(String movieName);

    /**
     * Tìm phim theo studio
     */
    List<Movie> findByStudioContainingIgnoreCaseAndStatus(String studio, Movie_Status status);

    /**
     * Tìm phim theo studio (tất cả status)
     */
    List<Movie> findByStudioContainingIgnoreCase(String studio);

    /**
     * Tìm phim có rating cao nhất
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.movieRate IS NOT NULL " +
            "ORDER BY m.movieRate DESC, m.movieName ASC")
    List<Movie> findTopRatedMovies();

    /**
     * Tìm phim có thời lượng dài nhất
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.duration IS NOT NULL " +
            "ORDER BY m.duration DESC")
    List<Movie> findLongestMovies();

    /**
     * Tìm phim có thời lượng ngắn nhất
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.duration IS NOT NULL " +
            "ORDER BY m.duration ASC")
    List<Movie> findShortestMovies();

    /**
     * Tìm phim theo năm phát hành
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE YEAR(m.startDate) = :year " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.startDate ASC")
    List<Movie> findByReleaseYear(@Param("year") Integer year);

    /**
     * Tìm phim không có rating
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.movieRate IS NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "ORDER BY m.movieName ASC")
    List<Movie> findMoviesWithoutRating();

    /**
     * Tìm phim không có thể loại
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.genres IS EMPTY " +
            "ORDER BY m.movieName ASC")
    List<Movie> findMoviesWithoutGenres();

    /**
     * Tìm phim không có diễn viên
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.actors IS EMPTY " +
            "ORDER BY m.movieName ASC")
    List<Movie> findMoviesWithoutActors();

    /**
     * Đếm tổng số phim
     */
    @Query("SELECT COUNT(m) FROM Movie m")
    long countAllMovies();

    /**
     * Tính rating trung bình của tất cả phim active
     */
    @Query("SELECT AVG(m.movieRate) FROM Movie m " +
            "WHERE m.movieRate IS NOT NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active")
    Double calculateAverageRating();

    /**
     * Tính thời lượng trung bình của tất cả phim active
     */
    @Query("SELECT AVG(m.duration) FROM Movie m " +
            "WHERE m.duration IS NOT NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active")
    Double calculateAverageDuration();

    /**
     * Tìm phim theo nhiều thể loại
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.genreID IN :genreIds " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY m.movieID " +
            "HAVING COUNT(DISTINCT g.genreID) = :genreCount " +
            "ORDER BY m.movieName ASC")
    List<Movie> findByMultipleGenres(@Param("genreIds") List<Integer> genreIds,
                                     @Param("genreCount") Long genreCount);

    /**
     * Tìm phim theo nhiều diễn viên
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE a.actorID IN :actorIds " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY m.movieID " +
            "HAVING COUNT(DISTINCT a.actorID) = :actorCount " +
            "ORDER BY m.movieName ASC")
    List<Movie> findByMultipleActors(@Param("actorIds") List<Integer> actorIds,
                                     @Param("actorCount") Long actorCount);

    /**
     * Tìm phim sắp hết hạn (trong vòng N ngày)
     * FIXED: Sử dụng LocalDateTime comparison thay vì DATE() function
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.endDate BETWEEN CURRENT_TIMESTAMP AND :endDate " +
            "ORDER BY m.endDate ASC")
    List<Movie> findMoviesEndingSoon(@Param("endDate") LocalDateTime endDateTime);

    /**
     * Tìm phim đã hết hạn nhưng vẫn có status Active
     * FIXED: Sử dụng LocalDateTime comparison thay vì DATE() function
     */
    @Query("SELECT m FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.endDate < CURRENT_TIMESTAMP " +
            "ORDER BY m.endDate DESC")
    List<Movie> findExpiredActiveMovies();

    /**
     * Thống kê phim theo tháng
     */
    @Query("SELECT YEAR(m.startDate), MONTH(m.startDate), COUNT(m) FROM Movie m " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY YEAR(m.startDate), MONTH(m.startDate) " +
            "ORDER BY YEAR(m.startDate) DESC, MONTH(m.startDate) DESC")
    List<Object[]> getMovieStatisticsByMonth();

    /**
     * Thống kê phim theo thể loại
     * FIXED: Sử dụng @ManyToMany relationship
     */
    @Query("SELECT g.genreName, COUNT(DISTINCT m) FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY g.genreID, g.genreName " +
            "ORDER BY COUNT(DISTINCT m) DESC")
    List<Object[]> getMovieStatisticsByGenre();
    /**
     * Tìm phim theo status với phân trang
     */
    @Query("SELECT m FROM Movie m WHERE m.status = :status")
    Page<Movie> findByStatus(@Param("status") Movie_Status status, Pageable pageable);

    /**
     * Tìm phim theo tên và status với phân trang
     */
    @Query("SELECT m FROM Movie m WHERE m.movieName LIKE %:keyword% AND m.status = :status")
    Page<Movie> findByMovieNameContainingIgnoreCaseAndStatus(
            @Param("keyword") String keyword,
            @Param("status") Movie_Status status,
            Pageable pageable);

    /**
     * Tìm phim theo danh sách ID và status với phân trang
     */
    @Query("SELECT m FROM Movie m WHERE m.movieID IN :movieIds AND m.status = :status")
    Page<Movie> findByMovieIDInAndStatus(
            @Param("movieIds") List<Integer> movieIds,
            @Param("status") Movie_Status status,
            Pageable pageable);




}