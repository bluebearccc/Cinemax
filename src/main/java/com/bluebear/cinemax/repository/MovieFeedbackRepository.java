package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieFeedbackRepository extends JpaRepository<MovieFeedback, Integer> {

    Page<MovieFeedback> findByMovieOrderByCreatedDateDesc(Movie movie, Pageable pageable);

    Page<MovieFeedback> findByMovieOrderByIdDesc(Movie movie, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT mf.movie.movieID) FROM MovieFeedback mf")
    long countMoviesWithFeedback();

}
