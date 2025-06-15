package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieFeedback;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieFeedbackRepository extends JpaRepository<MovieFeedback, Integer> {

    List<MovieFeedback> findByMovieOrderByCreatedDateDesc(Movie movie, Pageable pageable);

    List<MovieFeedback> findByMovie(Movie movie, Pageable pageable);

    long countByMovie(Movie movie);

    @Query("SELECT COUNT(DISTINCT mf.movie.movieID) FROM MovieFeedback mf")
    long countMoviesWithFeedback();

}
