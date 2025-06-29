package com.bluebear.cinemax.service.moviefeedbackcomment;

import com.bluebear.cinemax.dto.MovieFeedbackCommentDTO;
import com.bluebear.cinemax.entity.MovieFeedbackComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieFeedbackCommentService {
    Page<MovieFeedbackCommentDTO> getCommentsByFeedbackId(Integer feedbackId, Pageable pageable);

    MovieFeedbackCommentDTO createComment(MovieFeedbackCommentDTO dto);

    int countCommentByFeedbackId(int feedbackId);

    void deleteComment(Integer commentId);

    MovieFeedbackCommentDTO getById(Integer commentId);

    MovieFeedbackCommentDTO toDTO(MovieFeedbackComment comment);

    MovieFeedbackComment toEntity(MovieFeedbackCommentDTO dto);
}
