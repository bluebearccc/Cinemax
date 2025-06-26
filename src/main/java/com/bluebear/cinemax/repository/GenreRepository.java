package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    // Tìm thể loại theo tên
    List<Genre> findByGenreNameContainingIgnoreCase(String genreName);

    // Tìm thể loại theo phim
    @Query("SELECT DISTINCT g FROM Genre g JOIN g.movieGenres mg WHERE mg.movie.movieId = :movieId")
    List<Genre> findByMovieId(@Param("movieId") Integer movieId);

    // Tìm tất cả thể loại có sắp xếp theo tên
    List<Genre> findAllByOrderByGenreNameAsc();
}