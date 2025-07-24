package com.bluebear.cinemax.service.moviefeedbackcomment;

import com.bluebear.cinemax.dto.MovieFeedbackCommentDTO;
import com.bluebear.cinemax.entity.MovieFeedbackComment;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.MovieFeedbackCommentRepository;
import com.bluebear.cinemax.repository.MovieFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class MovieFeedbackCommentServiceImpl implements MovieFeedbackCommentService{

    @Autowired
    private MovieFeedbackCommentRepository commentRepository;
    @Autowired
    private MovieFeedbackRepository feedbackRepository;
    @Autowired
    private CustomerRepository customerRepository;

    public Page<MovieFeedbackCommentDTO> getCommentsByFeedbackId(Integer feedbackId, Pageable pageable) {
        return commentRepository.findByFeedback_IdOrderByCommentIdDesc(feedbackId, pageable)
                .map(this::toDTO);
    }

    public MovieFeedbackCommentDTO createComment(MovieFeedbackCommentDTO dto) {
        MovieFeedbackComment entity = toEntity(dto);
        MovieFeedbackComment saved = commentRepository.save(entity);
        return toDTO(saved);
    }

    public int countCommentByFeedbackId(int feedbackId) {
        return (int) commentRepository.countByFeedback_Id(feedbackId);
    }

    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

    public MovieFeedbackCommentDTO getById(Integer commentId) {
        return commentRepository.findById(commentId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public MovieFeedbackCommentDTO toDTO(MovieFeedbackComment comment) {
        return MovieFeedbackCommentDTO.builder()
                .commentId(comment.getCommentId())
                .feedbackId(comment.getFeedback().getId())
                .authorCustomerId(comment.getAuthor().getId())
                .repliedToCustomerId(comment.getRepliedTo() != null ? comment.getRepliedTo().getId() : null)
                .content(comment.getContent())
                .createdDate(comment.getCreatedDate())
                .authorName(comment.getAuthor().getFullName()) // Optional
                .repliedToName(comment.getRepliedTo() != null ? comment.getRepliedTo().getFullName() : null)
                .build();
    }

    public MovieFeedbackComment toEntity(MovieFeedbackCommentDTO dto) {
        MovieFeedbackComment comment = new MovieFeedbackComment();

        comment.setCommentId(dto.getCommentId());

        comment.setFeedback(feedbackRepository.findById(dto.getFeedbackId())
                .orElseThrow(() -> new RuntimeException("Feedback not found")));

        comment.setAuthor(customerRepository.findById(dto.getAuthorCustomerId())
                .orElseThrow(() -> new RuntimeException("Author not found")));

        if (dto.getRepliedToCustomerId() != null) {
            comment.setRepliedTo(customerRepository.findById(dto.getRepliedToCustomerId())
                    .orElseThrow(() -> new RuntimeException("RepliedTo user not found")));
        }

        comment.setContent(dto.getContent());

        if (dto.getCreatedDate() != null) {
            comment.setCreatedDate(dto.getCreatedDate());
        }

        return comment;
    }
}