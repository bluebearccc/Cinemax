package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.staff.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class MovieServiceImpl implements MovieService{
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public List<Movie> findAllShowingMovies() {
        return movieRepository.findAllShowingMovies() ;
    }

    @Override
    public List<Movie> searchMovieByName(String name) {
        return movieRepository.findAllByMovieName(name);
    }
}
