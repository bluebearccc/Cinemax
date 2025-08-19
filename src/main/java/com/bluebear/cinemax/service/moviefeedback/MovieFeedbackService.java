package com.bluebear.cinemax.service.moviefeedback;

import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.entity.MovieFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieFeedbackService {

    MovieFeedbackDTO create(MovieFeedbackDTO dto);

    MovieFeedbackDTO getById(Integer id);

    MovieFeedbackDTO update(Integer id, MovieFeedbackDTO dto);

    boolean delete(Integer id);

    Page<MovieFeedbackDTO> getAllByMovieIdSort(Integer movieId, Pageable pageable);

    Page<MovieFeedbackDTO> getAllByMovieId(Integer movieId, Pageable pageable);

    Page<MovieFeedbackDTO> getAllByMovieIdWithComments(Integer movieId, Pageable pageable);

    Page<MovieFeedbackDTO> getAllByMovieIdWithCommentCount(Integer movieId, Pageable pageable);

    MovieFeedbackDTO getFeedBackWithCommentByFeedbackId(Integer feedbackId);

    MovieFeedbackDTO getFeedBackWithCommentCountByFeedbackId(Integer feedbackId);

    int countDistinctMovieByFeedBack();

    MovieFeedbackDTO toDTO(MovieFeedback feedback);

    MovieFeedback toEntity(MovieFeedbackDTO dto);
}
