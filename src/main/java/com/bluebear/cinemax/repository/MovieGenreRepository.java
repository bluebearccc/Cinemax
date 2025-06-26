package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
    List<MovieGenre> findByGenreId(int genreId);

    List<MovieGenre> findByMovieId(int movieId);

}