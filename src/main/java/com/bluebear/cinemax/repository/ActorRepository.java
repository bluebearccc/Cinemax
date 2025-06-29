package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ActorRepository extends JpaRepository<Actor, Integer> {

    @Query("SELECT a FROM Actor a JOIN a.movies m WHERE m.movieID = :movieId")
    Page<Actor> findActorsByMovieId(Integer movieId, Pageable pageable);

    Page<Actor> findActorsByMovies(Movie movie, Pageable pageable);
}