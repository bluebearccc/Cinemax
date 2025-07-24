package com.bluebear.cinemax.repository.repos;

import com.bluebear.cinemax.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("genreAdminRepo")
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    // Basic Query Methods
    List<Genre> findByGenreNameContainingIgnoreCase(String genreName);
    boolean existsByGenreNameIgnoreCase(String genreName);
    boolean existsByGenreNameIgnoreCaseAndGenreIDNot(String genreName, Integer genreID);

    // Statistics Queries
    @Query(value = "SELECT COUNT(DISTINCT g.GenreID) FROM Genre g " +
            "INNER JOIN Movie_Genre mg ON g.GenreID = mg.GenreID", nativeQuery = true)
    long countGenresWithMovies();

    @Query(value = "SELECT TOP 1 g.* FROM Genre g " +
            "LEFT JOIN Movie_Genre mg ON g.GenreID = mg.GenreID " +
            "GROUP BY g.GenreID, g.GenreName " +
            "ORDER BY COUNT(mg.MovieID) DESC", nativeQuery = true)
    Optional<Genre> findMostPopularGenre();

    // Movie-Genre Association Queries
    @Query(value = "SELECT g.* FROM Genre g " +
            "INNER JOIN Movie_Genre mg ON g.GenreID = mg.GenreID " +
            "WHERE mg.MovieID = :movieId", nativeQuery = true)
    List<Genre> findGenresByMovieId(@Param("movieId") Integer movieId);

    // Movie Count Queries
    @Query(value = "SELECT COUNT(mg.MovieID) FROM Movie_Genre mg " +
            "WHERE mg.GenreID = :genreID", nativeQuery = true)
    Long countMoviesByGenreID(@Param("genreID") Integer genreID);

    @Query(value = "SELECT g.GenreID, COUNT(mg.MovieID) as movieCount " +
            "FROM Genre g LEFT JOIN Movie_Genre mg ON g.GenreID = mg.GenreID " +
            "GROUP BY g.GenreID ORDER BY g.GenreID", nativeQuery = true)
    List<Object[]> findAllGenreMovieCounts();

    @Query(value = "SELECT g.GenreID, COUNT(mg.MovieID) as movieCount " +
            "FROM Genre g LEFT JOIN Movie_Genre mg ON g.GenreID = mg.GenreID " +
            "WHERE LOWER(g.GenreName) LIKE LOWER('%' + :keyword + '%') " +
            "GROUP BY g.GenreID ORDER BY g.GenreID", nativeQuery = true)
    List<Object[]> findGenreMovieCountsByKeyword(@Param("keyword") String keyword);
}