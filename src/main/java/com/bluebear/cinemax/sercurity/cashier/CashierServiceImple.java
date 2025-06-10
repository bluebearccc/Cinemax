package com.bluebear.cinemax.sercurity.cashier;

import com.bluebear.cinemax.dto.cashier.MovieDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.cashier.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CashierServiceImple implements CashierService{

    private MovieRepository movieRepository;

    @Autowired
    public CashierServiceImple(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }


    private MovieDTO convertToDto(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());
        dto.setMovieRate(movie.getMovieRate());
        dto.setActor(movie.getActor());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setStatus(movie.getStatus());
        return dto;
    }

    @Override
    public Page<MovieDTO> getMovieAvailable(Movie.MovieStatus status, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return movieRepository.findAllByStatusOrderByMovieName(status, pageRequest).map(this::convertToDto);
    }

    @Override
    public MovieDTO getMovieById(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie == null) {
            return null;
        }
        return convertToDto(movie);
    }
}
