package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {

    // Tìm tất cả MovieGenre theo movieId
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.movie.movieId = :movieId")
    List<MovieGenre> findByMovieId(@Param("movieId") Integer movieId);

    // Tìm tất cả MovieGenre theo genreId
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.genre.genreId = :genreId")
    List<MovieGenre> findByGenreId(@Param("genreId") Integer genreId);

    // Tìm MovieGenre theo movieId và genreId
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.movie.movieId = :movieId AND mg.genre.genreId = :genreId")
    MovieGenre findByMovieIdAndGenreId(@Param("movieId") Integer movieId, @Param("genreId") Integer genreId);

    // Kiểm tra tồn tại MovieGenre theo movieId và genreId
    @Query("SELECT COUNT(mg) > 0 FROM MovieGenre mg WHERE mg.movie.movieId = :movieId AND mg.genre.genreId = :genreId")
    boolean existsByMovieIdAndGenreId(@Param("movieId") Integer movieId, @Param("genreId") Integer genreId);

    // Xóa tất cả MovieGenre theo movieId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.movieId = :movieId")
    void deleteByMovieId(@Param("movieId") Integer movieId);

    // Xóa MovieGenre theo movieId và genreId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.movieId = :movieId AND mg.genre.genreId = :genreId")
    void deleteByMovieIdAndGenreId(@Param("movieId") Integer movieId, @Param("genreId") Integer genreId);

    // Xóa tất cả MovieGenre theo genreId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.genre.genreId = :genreId")
    void deleteByGenreId(@Param("genreId") Integer genreId);

    // Đếm số phim theo genreId
    @Query("SELECT COUNT(DISTINCT mg.movie.movieId) FROM MovieGenre mg WHERE mg.genre.genreId = :genreId")
    long countByGenreId(@Param("genreId") Integer genreId);

    // Đếm số genre theo movieId
    @Query("SELECT COUNT(mg) FROM MovieGenre mg WHERE mg.movie.movieId = :movieId")
    long countByMovieId(@Param("movieId") Integer movieId);

    // Lấy tất cả movieId theo genreId
    @Query("SELECT DISTINCT mg.movie.movieId FROM MovieGenre mg WHERE mg.genre.genreId = :genreId")
    List<Integer> findMovieIdsByGenreId(@Param("genreId") Integer genreId);

    // Lấy tất cả genreId theo movieId
    @Query("SELECT DISTINCT mg.genre.genreId FROM MovieGenre mg WHERE mg.movie.movieId = :movieId")
    List<Integer> findGenreIdsByMovieId(@Param("movieId") Integer movieId);

    // Tìm các phim có cùng genre với phim cho trước (trừ chính nó)
    @Query("SELECT DISTINCT mg2.movie.movieId FROM MovieGenre mg1 " +
            "JOIN MovieGenre mg2 ON mg1.genre.genreId = mg2.genre.genreId " +
            "WHERE mg1.movie.movieId = :movieId AND mg2.movie.movieId != :movieId")
    List<Integer> findRelatedMovieIds(@Param("movieId") Integer movieId);

    // Tìm genre phổ biến nhất (có nhiều phim nhất)
    @Query("SELECT mg.genre.genreId, COUNT(mg.movie.movieId) as movieCount " +
            "FROM MovieGenre mg " +
            "GROUP BY mg.genre.genreId " +
            "ORDER BY movieCount DESC")
    List<Object[]> findMostPopularGenres();

    // Kiểm tra phim có genre nào không
    @Query("SELECT COUNT(mg) > 0 FROM MovieGenre mg WHERE mg.movie.movieId = :movieId")
    boolean movieHasAnyGenre(@Param("movieId") Integer movieId);

    // Lấy danh sách phim không có genre nào
    @Query("SELECT m.movieId FROM Movie m WHERE m.movieId NOT IN " +
            "(SELECT DISTINCT mg.movie.movieId FROM MovieGenre mg)")
    List<Integer> findMoviesWithoutGenres();

    // Thống kê số lượng phim theo từng genre
    @Query("SELECT g.genreName, COUNT(mg.movie.movieId) " +
            "FROM Genre g LEFT JOIN MovieGenre mg ON g.genreId = mg.genre.genreId " +
            "GROUP BY g.genreId, g.genreName " +
            "ORDER BY COUNT(mg.movie.movieId) DESC")
    List<Object[]> getGenreStatistics();
}