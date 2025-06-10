package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {

    Page<Movie> findAllByStatusOrderByMovieName(Movie.MovieStatus status, Pageable pageable);

    // Tìm phim đang chiếu
    List<Movie> findByStatusAndStartDateBeforeAndEndDateAfterOrderByMovieName(Movie.MovieStatus status, LocalDate currentDate1, LocalDate currentDate2);
    // Tìm phim theo tên
    List<Movie> findByMovieNameContainingIgnoreCaseAndStatus(String movieName, Movie.MovieStatus status);

    // Tìm phim theo thể loại
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.movieGenres mg WHERE mg.genre.genreId = :genreId AND m.status = :status")
    List<Movie> findByGenreAndStatus(@Param("genreId") Integer genreId, @Param("status") Movie.MovieStatus status);
}