package com.bluebear.cinemax.sercurity.cashier;

import com.bluebear.cinemax.dto.cashier.MovieDTO;
import com.bluebear.cinemax.entity.Movie;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CashierService {
    Page<MovieDTO> getMovieAvailable(Movie.MovieStatus status, Integer page, Integer size);

    MovieDTO getMovieById(Integer movieId);
}
