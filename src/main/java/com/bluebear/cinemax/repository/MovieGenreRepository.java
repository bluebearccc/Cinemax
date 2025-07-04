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

    // Tìm tất cả MovieGenre theo movieID
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.movie.movieID = :movieID")
    List<MovieGenre> findBymovieID(@Param("movieID") Integer movieID);

    // Tìm tất cả MovieGenre theo genreID
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.genre.genreID = :genreID")
    List<MovieGenre> findBygenreID(@Param("genreID") Integer genreID);

    // Tìm MovieGenre theo movieID và genreID
    @Query("SELECT mg FROM MovieGenre mg WHERE mg.movie.movieID = :movieID AND mg.genre.genreID = :genreID")
    MovieGenre findBymovieIDAndgenreID(@Param("movieID") Integer movieID, @Param("genreID") Integer genreID);

    // Kiểm tra tồn tại MovieGenre theo movieID và genreID
    @Query("SELECT COUNT(mg) > 0 FROM MovieGenre mg WHERE mg.movie.movieID = :movieID AND mg.genre.genreID = :genreID")
    boolean existsBymovieIDAndgenreID(@Param("movieID") Integer movieID, @Param("genreID") Integer genreID);

    // Xóa tất cả MovieGenre theo movieID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.movieID = :movieID")
    void deleteBymovieID(@Param("movieID") Integer movieID);

    // Xóa MovieGenre theo movieID và genreID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.movieID = :movieID AND mg.genre.genreID = :genreID")
    void deleteBymovieIDAndgenreID(@Param("movieID") Integer movieID, @Param("genreID") Integer genreID);

    // Xóa tất cả MovieGenre theo genreID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieGenre mg WHERE mg.genre.genreID = :genreID")
    void deleteBygenreID(@Param("genreID") Integer genreID);

    // Đếm số phim theo genreID
    @Query("SELECT COUNT(DISTINCT mg.movie.movieID) FROM MovieGenre mg WHERE mg.genre.genreID = :genreID")
    long countBygenreID(@Param("genreID") Integer genreID);

    // Đếm số genre theo movieID
    @Query("SELECT COUNT(mg) FROM MovieGenre mg WHERE mg.movie.movieID = :movieID")
    long countBymovieID(@Param("movieID") Integer movieID);

    // Lấy tất cả movieID theo genreID
    @Query("SELECT DISTINCT mg.movie.movieID FROM MovieGenre mg WHERE mg.genre.genreID = :genreID")
    List<Integer> findmovieIDsBygenreID(@Param("genreID") Integer genreID);

    // Lấy tất cả genreID theo movieID
    @Query("SELECT DISTINCT mg.genre.genreID FROM MovieGenre mg WHERE mg.movie.movieID = :movieID")
    List<Integer> findgenreIDsBymovieID(@Param("movieID") Integer movieID);

    // Tìm các phim có cùng genre với phim cho trước (trừ chính nó)
    @Query("SELECT DISTINCT mg2.movie.movieID FROM MovieGenre mg1 " +
            "JOIN MovieGenre mg2 ON mg1.genre.genreID = mg2.genre.genreID " +
            "WHERE mg1.movie.movieID = :movieID AND mg2.movie.movieID != :movieID")
    List<Integer> findRelatedmovieIDs(@Param("movieID") Integer movieID);

    // Tìm genre phổ biến nhất (có nhiều phim nhất)
    @Query("SELECT mg.genre.genreID, COUNT(mg.movie.movieID) as movieCount " +
            "FROM MovieGenre mg " +
            "GROUP BY mg.genre.genreID " +
            "ORDER BY movieCount DESC")
    List<Object[]> findMostPopularGenres();

    // Kiểm tra phim có genre nào không
    @Query("SELECT COUNT(mg) > 0 FROM MovieGenre mg WHERE mg.movie.movieID = :movieID")
    boolean movieHasAnyGenre(@Param("movieID") Integer movieID);

    // Lấy danh sách phim không có genre nào
    @Query("SELECT m.movieID FROM Movie m WHERE m.movieID NOT IN " +
            "(SELECT DISTINCT mg.movie.movieID FROM MovieGenre mg)")
    List<Integer> findMoviesWithoutGenres();

    // Thống kê số lượng phim theo từng genre
    @Query("SELECT g.genreName, COUNT(mg.movie.movieID) " +
            "FROM Genre g LEFT JOIN MovieGenre mg ON g.genreID = mg.genre.genreID " +
            "GROUP BY g.genreID, g.genreName " +
            "ORDER BY COUNT(mg.movie.movieID) DESC")
    List<Object[]> getGenreStatistics();
}