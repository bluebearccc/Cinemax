package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.MovieFeedbackComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieFeedbackCommentRepository extends JpaRepository<MovieFeedbackComment, Integer> {
    Page<MovieFeedbackComment> findByFeedback_IdOrderByCommentIdDesc(Integer feedbackId, Pageable pageable);

    long countByFeedback_Id(Integer feedbackId);
}
