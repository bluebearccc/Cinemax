package com.bluebear.cinemax.repository.staff;

import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    @Query(value="select * from Movie m where m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllShowingMovies();

    @Query(value="select * from Movie m where LOWER(m.MovieName) LIKE LOWER(CONCAT('%', :name, '%')) AND m.StartDate <= GETDATE() And m.EndDate >= GETDATE()", nativeQuery = true)
    List<Movie> findAllByMovieName(@Param("name") String name);
}
