package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    /**
     * Tìm tất cả thể loại và sắp xếp theo tên
     */
    List<Genre> findAllByOrderByGenreNameAsc();

    /**
     * Tìm thể loại theo tên (tìm kiếm không phân biệt hoa thường)
     */
    List<Genre> findByGenreNameContainingIgnoreCase(String genreName);

    /**
     * Tìm thể loại theo tên chính xác
     */
    Genre findByGenreName(String genreName);

    /**
     * Kiểm tra thể loại có tồn tại với tên này không
     */
    boolean existsByGenreName(String genreName);

    /**
     * Tìm thể loại theo phim ID (thông qua bảng trung gian MovieGenre)
     */
    @Query("SELECT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "WHERE mg.movie.movieID = :movieId " +
            "ORDER BY g.genreName ASC")
    List<Genre> findByMovieId(@Param("movieId") Integer movieId);

    /**
     * Đếm số phim theo thể loại
     */
    @Query("SELECT g.genreID, g.genreName, COUNT(mg.movie) FROM Genre g " +
            "LEFT JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "GROUP BY g.genreID, g.genreName " +
            "ORDER BY COUNT(mg.movie) DESC")
    List<Object[]> countMoviesByGenre();

    /**
     * Tìm thể loại phổ biến nhất (có nhiều phim nhất)
     */
    @Query("SELECT g FROM Genre g " +
            "LEFT JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "GROUP BY g.genreID " +
            "ORDER BY COUNT(mg.movie) DESC")
    List<Genre> findGenresOrderByMovieCount();

    /**
     * Tìm thể loại có phim đang chiếu
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.startDate <= CURRENT_TIMESTAMP " +
            "AND m.endDate >= CURRENT_TIMESTAMP " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresWithActiveMovies();

    /**
     * Tìm thể loại có phim sắp chiếu
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.startDate > CURRENT_TIMESTAMP " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresWithUpcomingMovies();

    /**
     * Tìm thể loại theo studio
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE UPPER(m.studio) LIKE UPPER(CONCAT('%', :studio, '%')) " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresByStudio(@Param("studio") String studio);

    /**
     * Tìm thể loại theo rating trung bình cao
     */
    @Query("SELECT g, AVG(m.movieRate) as avgRating FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.movieRate IS NOT NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY g.genreID " +
            "HAVING AVG(m.movieRate) >= :minRating " +
            "ORDER BY avgRating DESC")
    List<Object[]> findGenresByMinAverageRating(@Param("minRating") Double minRating);

    /**
     * Tìm thể loại theo khoảng thời gian
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.startDate BETWEEN :startDate AND :endDate " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresByReleasePeriod(@Param("startDate") java.time.LocalDateTime startDate,
                                          @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Tìm thể loại mới (chưa có phim nào)
     */
    @Query("SELECT g FROM Genre g " +
            "WHERE NOT EXISTS (SELECT mg FROM MovieGenre mg WHERE mg.genre.genreID = g.genreID) " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresWithoutMovies();

    /**
     * Tìm thể loại theo diễn viên
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "JOIN MovieActor ma ON m.movieID = ma.movie.movieID " +
            "WHERE ma.actor.actorID = :actorID " +
            "ORDER BY g.genreName ASC")
    List<Genre> findGenresByactorID(@Param("actorID") Integer actorID);

    /**
     * Đếm số phim active theo thể loại
     */
    @Query("SELECT COUNT(mg.movie) FROM MovieGenre mg " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE mg.genre.genreID = :genreId " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active")
    Long countActiveMoviesByGenre(@Param("genreId") Integer genreId);

    /**
     * Tìm top thể loại theo rating cao nhất
     */
    @Query("SELECT g, AVG(m.movieRate) as avgRating, COUNT(mg.movie) as movieCount FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.movieRate IS NOT NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY g.genreID " +
            "HAVING COUNT(mg.movie) >= :minMovieCount " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopGenresByRating(@Param("minMovieCount") Long minMovieCount);

    /**
     * Tìm kiếm thể loại theo nhiều tiêu chí
     */
    @Query("SELECT DISTINCT g FROM Genre g " +
            "LEFT JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "LEFT JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE (:name IS NULL OR UPPER(g.genreName) LIKE UPPER(CONCAT('%', :name, '%'))) " +
            "AND (:hasActiveMovies IS NULL OR " +
            "     (:hasActiveMovies = true AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active) OR " +
            "     (:hasActiveMovies = false AND m.movieID IS NULL)) " +
            "ORDER BY g.genreName ASC")
    List<Genre> searchGenresWithCriteria(@Param("name") String name,
                                         @Param("hasActiveMovies") Boolean hasActiveMovies);

    /**
     * Tìm thể loại có rating trung bình cao nhất
     */
    @Query("SELECT g FROM Genre g " +
            "JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "JOIN Movie m ON mg.movie.movieID = m.movieID " +
            "WHERE m.movieRate IS NOT NULL " +
            "GROUP BY g.genreID " +
            "ORDER BY AVG(m.movieRate) DESC")
    List<Genre> findGenresOrderByAverageRating();
}