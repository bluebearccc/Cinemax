package com.bluebear.cinemax.service.moviefeedback;

import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.entity.MovieFeedback;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieFeedbackService {
    // CRUD methods
    MovieFeedbackDTO create(MovieFeedbackDTO dto);

    MovieFeedbackDTO getById(Integer id);

    MovieFeedbackDTO update(Integer id, MovieFeedbackDTO dto);

    boolean delete(Integer id);

    // Feedback listing methods
    List<MovieFeedbackDTO> getAllByMovieIdSort(Integer movieId, Pageable pageable);

    List<MovieFeedbackDTO> getAllByMovieId(Integer movieId, Pageable pageable);

    // Count methods
    int countFeedbackByMovieId(Integer movieId);

    int countDistinctMovieByFeedBack();

    // Mapping methods
    MovieFeedbackDTO toDTO(MovieFeedback feedback);

    MovieFeedback fromDTO(MovieFeedbackDTO dto);
}
