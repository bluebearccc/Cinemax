package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Movie;

import java.util.List;

public interface MovieService {

    public List<MovieDTO> findAllShowingMovies();

    public List<MovieDTO> searchExistingMovieByName(String name);

    public MovieDTO getMovieById(Integer id);
}
