package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {

    /**
     * Tìm tất cả thể loại và sắp xếp theo tên
     */
    List<Genre> findAllByOrderByGenreNameAsc();

    /**
     * Tìm thể loại theo phim ID - SIMPLIFIED sử dụng @ManyToMany relationship
     */
    @Query("SELECT g FROM Genre g " +
            "JOIN g.movies m " +
            "WHERE m.movieID = :movieId " +
            "ORDER BY g.genreName ASC")
    List<Genre> findByMovieId(@Param("movieId") Integer movieId);
}