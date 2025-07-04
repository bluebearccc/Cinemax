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

    // Tìm tất cả MovieActor theo movieID
    @Query("SELECT ma FROM MovieActor ma WHERE ma.movie.movieID = :movieID")
    List<MovieActor> findBymovieID(@Param("movieID") Integer movieID);

    // Tìm tất cả MovieActor theo actorID
    @Query("SELECT ma FROM MovieActor ma WHERE ma.actor.actorID = :actorID")
    List<MovieActor> findByactorID(@Param("actorID") Integer actorID);

    // Tìm MovieActor theo movieID và actorID
    @Query("SELECT ma FROM MovieActor ma WHERE ma.movie.movieID = :movieID AND ma.actor.actorID = :actorID")
    MovieActor findBymovieIDAndactorID(@Param("movieID") Integer movieID, @Param("actorID") Integer actorID);

    // Kiểm tra tồn tại MovieActor theo movieID và actorID
    @Query("SELECT COUNT(ma) > 0 FROM MovieActor ma WHERE ma.movie.movieID = :movieID AND ma.actor.actorID = :actorID")
    boolean existsBymovieIDAndactorID(@Param("movieID") Integer movieID, @Param("actorID") Integer actorID);

    // Xóa tất cả MovieActor theo movieID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.movie.movieID = :movieID")
    void deleteBymovieID(@Param("movieID") Integer movieID);

    // Xóa MovieActor theo movieID và actorID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.movie.movieID = :movieID AND ma.actor.actorID = :actorID")
    void deleteBymovieIDAndactorID(@Param("movieID") Integer movieID, @Param("actorID") Integer actorID);

    // Xóa tất cả MovieActor theo actorID
    @Modifying
    @Transactional
    @Query("DELETE FROM MovieActor ma WHERE ma.actor.actorID = :actorID")
    void deleteByactorID(@Param("actorID") Integer actorID);

    // Đếm số phim theo actorID
    @Query("SELECT COUNT(DISTINCT ma.movie.movieID) FROM MovieActor ma WHERE ma.actor.actorID = :actorID")
    long countByactorID(@Param("actorID") Integer actorID);

    // Đếm số actor theo movieID
    @Query("SELECT COUNT(ma) FROM MovieActor ma WHERE ma.movie.movieID = :movieID")
    long countBymovieID(@Param("movieID") Integer movieID);

    // Lấy tất cả movieID theo actorID
    @Query("SELECT DISTINCT ma.movie.movieID FROM MovieActor ma WHERE ma.actor.actorID = :actorID")
    List<Integer> findmovieIDsByactorID(@Param("actorID") Integer actorID);

    // Lấy tất cả actorID theo movieID
    @Query("SELECT DISTINCT ma.actor.actorID FROM MovieActor ma WHERE ma.movie.movieID = :movieID")
    List<Integer> findactorIDsBymovieID(@Param("movieID") Integer movieID);

    // Tìm các phim có cùng actor với phim cho trước (trừ chính nó)
    @Query("SELECT DISTINCT ma2.movie.movieID FROM MovieActor ma1 " +
            "JOIN MovieActor ma2 ON ma1.actor.actorID = ma2.actor.actorID " +
            "WHERE ma1.movie.movieID = :movieID AND ma2.movie.movieID != :movieID")
    List<Integer> findRelatedmovieIDsByActor(@Param("movieID") Integer movieID);

    // Tìm actor phổ biến nhất (tham gia nhiều phim nhất)
    @Query("SELECT ma.actor.actorID, COUNT(ma.movie.movieID) as movieCount " +
            "FROM MovieActor ma " +
            "GROUP BY ma.actor.actorID " +
            "ORDER BY movieCount DESC")
    List<Object[]> findMostActiveActors();

    // Kiểm tra phim có actor nào không
    @Query("SELECT COUNT(ma) > 0 FROM MovieActor ma WHERE ma.movie.movieID = :movieID")
    boolean movieHasAnyActor(@Param("movieID") Integer movieID);

    // Lấy danh sách phim không có actor nào
    @Query("SELECT m.movieID FROM Movie m WHERE m.movieID NOT IN " +
            "(SELECT DISTINCT ma.movie.movieID FROM MovieActor ma)")
    List<Integer> findMoviesWithoutActors();

    // Thống kê số lượng phim theo từng actor
    @Query("SELECT a.actorName, COUNT(ma.movie.movieID) " +
            "FROM Actor a LEFT JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "GROUP BY a.actorID, a.actorName " +
            "ORDER BY COUNT(ma.movie.movieID) DESC")
    List<Object[]> getActorStatistics();

    // Tìm cặp actor thường xuyên đóng chung phim
    @Query("SELECT ma1.actor.actorID, ma2.actor.actorID, COUNT(ma1.movie.movieID) as movieCount " +
            "FROM MovieActor ma1 " +
            "JOIN MovieActor ma2 ON ma1.movie.movieID = ma2.movie.movieID " +
            "WHERE ma1.actor.actorID < ma2.actor.actorID " +
            "GROUP BY ma1.actor.actorID, ma2.actor.actorID " +
            "HAVING COUNT(ma1.movie.movieID) > 1 " +
            "ORDER BY movieCount DESC")
    List<Object[]> findFrequentActorPairs();

    // Lấy top actors theo số lượng phim active
    @Query("SELECT ma.actor, COUNT(ma.movie.movieID) as movieCount " +
            "FROM MovieActor ma " +
            "WHERE ma.movie.status = 'Active' " +
            "GROUP BY ma.actor " +
            "ORDER BY movieCount DESC")
    List<Object[]> getTopActorsByActiveMovies();
}