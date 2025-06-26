package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.MovieActor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MovieActorRepository extends JpaRepository<MovieActor, Integer> {

    // Tìm tất cả MovieActor theo movieId
    @Query("SELECT ma FROM MovieActor ma WHERE ma.movie.movieId = :movieId")
    List<MovieActor> findByMovieId(@Param("movieId") Integer movieId);

    // Tìm tất cả MovieActor theo actorId
    @Query("SELECT ma FROM MovieActor ma WHERE ma.actor.actorId = :actorId")
    List<MovieActor> findByActorId(@Param("actorId") Integer actorId);

    // Tìm MovieActor theo movieId và actorId
    @Query("SELECT ma FROM MovieActor ma WHERE ma.movie.movieId = :movieId AND ma.actor.actorId = :actorId")
    MovieActor findByMovieIdAndActorId(@Param("movieId") Integer movieId, @Param("actorId") Integer actorId);

    // Kiểm tra tồn tại MovieActor theo movieId và actorId
    @Query("SELECT COUNT(ma) > 0 FROM MovieActor ma WHERE ma.movie.movieId = :movieId AND ma.actor.actorId = :actorId")
    boolean existsByMovieIdAndActorId(@Param("movieId") Integer movieId, @Param("actorId") Integer actorId);

    // Xóa tất cả MovieActor theo movieId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.movie.movieId = :movieId")
    void deleteByMovieId(@Param("movieId") Integer movieId);

    // Xóa MovieActor theo movieId và actorId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.movie.movieId = :movieId AND ma.actor.actorId = :actorId")
    void deleteByMovieIdAndActorId(@Param("movieId") Integer movieId, @Param("actorId") Integer actorId);

    // Xóa tất cả MovieActor theo actorId
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.actor.actorId = :actorId")
    void deleteByActorId(@Param("actorId") Integer actorId);

    // Đếm số phim theo actorId
    @Query("SELECT COUNT(DISTINCT ma.movie.movieId) FROM MovieActor ma WHERE ma.actor.actorId = :actorId")
    long countByActorId(@Param("actorId") Integer actorId);

    // Đếm số actor theo movieId
    @Query("SELECT COUNT(ma) FROM MovieActor ma WHERE ma.movie.movieId = :movieId")
    long countByMovieId(@Param("movieId") Integer movieId);

    // Lấy tất cả movieId theo actorId
    @Query("SELECT DISTINCT ma.movie.movieId FROM MovieActor ma WHERE ma.actor.actorId = :actorId")
    List<Integer> findMovieIdsByActorId(@Param("actorId") Integer actorId);

    // Lấy tất cả actorId theo movieId
    @Query("SELECT DISTINCT ma.actor.actorId FROM MovieActor ma WHERE ma.movie.movieId = :movieId")
    List<Integer> findActorIdsByMovieId(@Param("movieId") Integer movieId);

    // Tìm các phim có cùng actor với phim cho trước (trừ chính nó)
    @Query("SELECT DISTINCT ma2.movie.movieId FROM MovieActor ma1 " +
            "JOIN MovieActor ma2 ON ma1.actor.actorId = ma2.actor.actorId " +
            "WHERE ma1.movie.movieId = :movieId AND ma2.movie.movieId != :movieId")
    List<Integer> findRelatedMovieIdsByActor(@Param("movieId") Integer movieId);

    // Tìm actor phổ biến nhất (tham gia nhiều phim nhất)
    @Query("SELECT ma.actor.actorId, COUNT(ma.movie.movieId) as movieCount " +
            "FROM MovieActor ma " +
            "GROUP BY ma.actor.actorId " +
            "ORDER BY movieCount DESC")
    List<Object[]> findMostActiveActors();

    // Kiểm tra phim có actor nào không
    @Query("SELECT COUNT(ma) > 0 FROM MovieActor ma WHERE ma.movie.movieId = :movieId")
    boolean movieHasAnyActor(@Param("movieId") Integer movieId);

    // Lấy danh sách phim không có actor nào
    @Query("SELECT m.movieId FROM Movie m WHERE m.movieId NOT IN " +
            "(SELECT DISTINCT ma.movie.movieId FROM MovieActor ma)")
    List<Integer> findMoviesWithoutActors();

    // Thống kê số lượng phim theo từng actor
    @Query("SELECT a.actorName, COUNT(ma.movie.movieId) " +
            "FROM Actor a LEFT JOIN MovieActor ma ON a.actorId = ma.actor.actorId " +
            "GROUP BY a.actorId, a.actorName " +
            "ORDER BY COUNT(ma.movie.movieId) DESC")
    List<Object[]> getActorStatistics();

    // Tìm cặp actor thường xuyên đóng chung phim
    @Query("SELECT ma1.actor.actorId, ma2.actor.actorId, COUNT(ma1.movie.movieId) as movieCount " +
            "FROM MovieActor ma1 " +
            "JOIN MovieActor ma2 ON ma1.movie.movieId = ma2.movie.movieId " +
            "WHERE ma1.actor.actorId < ma2.actor.actorId " +
            "GROUP BY ma1.actor.actorId, ma2.actor.actorId " +
            "HAVING COUNT(ma1.movie.movieId) > 1 " +
            "ORDER BY movieCount DESC")
    List<Object[]> findFrequentActorPairs();

    // Lấy top actors theo số lượng phim active
    @Query("SELECT ma.actor, COUNT(ma.movie.movieId) as movieCount " +
            "FROM MovieActor ma " +
            "WHERE ma.movie.status = 'Active' " +
            "GROUP BY ma.actor " +
            "ORDER BY movieCount DESC")
    List<Object[]> getTopActorsByActiveMovies();
}