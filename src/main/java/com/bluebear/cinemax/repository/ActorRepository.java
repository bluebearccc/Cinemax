package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer> {

    /**
     * Tìm tất cả diễn viên và sắp xếp theo tên
     */
    List<Actor> findAllByOrderByActorNameAsc();

    /**
     * Tìm diễn viên theo tên (tìm kiếm không phân biệt hoa thường)
     */
    List<Actor> findByActorNameContainingIgnoreCase(String actorName);

    /**
     * Tìm diễn viên theo tên chính xác
     */
    Actor findByActorName(String actorName);

    /**
     * Kiểm tra diễn viên có tồn tại với tên này không
     */
    boolean existsByActorName(String actorName);

    /**
     * Tìm diễn viên theo phim ID (thông qua bảng trung gian MovieActor)
     */
    @Query("SELECT a FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "WHERE ma.movie.movieID = :movieId " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsByMovieId(@Param("movieId") Integer movieId);

    /**
     * Đếm số phim của mỗi diễn viên
     */
    @Query("SELECT a.actorID, COUNT(ma.movie) FROM Actor a " +
            "LEFT JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "GROUP BY a.actorID")
    List<Object[]> countMoviesByActor();

    /**
     * Tìm diễn viên có nhiều phim nhất
     */
    @Query("SELECT a FROM Actor a " +
            "LEFT JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "GROUP BY a.actorID " +
            "ORDER BY COUNT(ma.movie) DESC")
    List<Actor> findActorsOrderByMovieCount();

    /**
     * Tìm diễn viên theo studio (thông qua phim)
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE UPPER(m.studio) LIKE UPPER(CONCAT('%', :studio, '%')) " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsByStudio(@Param("studio") String studio);

    /**
     * Tìm diễn viên theo thể loại phim
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "JOIN MovieGenre mg ON m.movieID = mg.movie.movieID " +
            "WHERE mg.genre.genreID = :genreId " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsByGenreId(@Param("genreId") Integer genreId);

    /**
     * Tìm diễn viên có phim đang chiếu
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "AND m.startDate <= CURRENT_TIMESTAMP " +
            "AND m.endDate >= CURRENT_TIMESTAMP " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsWithActiveMovies();

    /**
     * Tìm diễn viên theo khoảng thời gian ra mắt phim
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE m.startDate BETWEEN :startDate AND :endDate " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsByMovieReleasePeriod(@Param("startDate") java.time.LocalDateTime startDate,
                                               @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Tìm top diễn viên theo rating trung bình của phim
     */
    @Query("SELECT a, AVG(m.movieRate) as avgRating FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE m.movieRate IS NOT NULL " +
            "AND m.status = com.bluebear.cinemax.enumtype.Movie_Status.Active " +
            "GROUP BY a.actorID " +
            "ORDER BY avgRating DESC")
    List<Object[]> findTopActorsByAverageMovieRating();

    /**
     * Tìm diễn viên mới (chưa có phim nào)
     */
    @Query("SELECT a FROM Actor a " +
            "WHERE NOT EXISTS (SELECT ma FROM MovieActor ma WHERE ma.actor.actorID = a.actorID) " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsWithoutMovies();

    /**
     * Tìm kiếm diễn viên với nhiều tiêu chí
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "LEFT JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "LEFT JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE (:name IS NULL OR UPPER(a.actorName) LIKE UPPER(CONCAT('%', :name, '%'))) " +
            "AND (:studio IS NULL OR UPPER(m.studio) LIKE UPPER(CONCAT('%', :studio, '%'))) " +
            "AND (:hasMovies IS NULL OR " +
            "     (:hasMovies = true AND ma.actor.actorID IS NOT NULL) OR " +
            "     (:hasMovies = false AND ma.actor.actorID IS NULL)) " +
            "ORDER BY a.actorName ASC")
    List<Actor> searchActorsWithCriteria(@Param("name") String name,
                                         @Param("studio") String studio,
                                         @Param("hasMovies") Boolean hasMovies);

    /**
     * Đếm số diễn viên theo trạng thái phim
     */
    @Query("SELECT COUNT(DISTINCT a.actorID) FROM Actor a " +
            "JOIN MovieActor ma ON a.actorID = ma.actor.actorID " +
            "JOIN Movie m ON ma.movie.movieID = m.movieID " +
            "WHERE m.status = :status")
    Long countActorsByMovieStatus(@Param("status") com.bluebear.cinemax.enumtype.Movie_Status status);

    /**
     * Tìm diễn viên có hình ảnh null hoặc rỗng
     */
    @Query("SELECT a FROM Actor a " +
            "WHERE a.image IS NULL OR a.image = '' " +
            "ORDER BY a.actorName ASC")
    List<Actor> findActorsWithoutImage();
}