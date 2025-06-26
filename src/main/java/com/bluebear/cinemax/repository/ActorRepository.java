package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer> {

    // Tìm diễn viên theo tên
    List<Actor> findByActorNameContainingIgnoreCase(String actorName);

    // Tìm diễn viên theo phim
    @Query("SELECT DISTINCT a FROM Actor a JOIN a.movieActors ma WHERE ma.movie.movieId = :movieId")
    List<Actor> findByMovieId(@Param("movieId") Integer movieId);

    // Tìm tất cả diễn viên có sắp xếp theo tên
    List<Actor> findAllByOrderByActorNameAsc();

    // Tìm diễn viên có tham gia phim đang active
    @Query("SELECT DISTINCT a FROM Actor a JOIN a.movieActors ma WHERE ma.movie.status = 'Active'")
    List<Actor> findActorsInActiveMovies();

    // Đếm số phim của diễn viên
    @Query("SELECT COUNT(ma) FROM MovieActor ma WHERE ma.actor.actorId = :actorId AND ma.movie.status = 'Active'")
    long countMoviesByActorId(@Param("actorId") Integer actorId);
}