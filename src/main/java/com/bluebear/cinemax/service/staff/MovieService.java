package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Movie;

import java.util.List;

public interface MovieService {

    public List<Movie> findAllShowingMovies();

    public List<Movie> searchMovieByName(String name);
}
