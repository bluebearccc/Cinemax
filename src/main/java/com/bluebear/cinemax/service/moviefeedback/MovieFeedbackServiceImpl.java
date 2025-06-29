package com.bluebear.cinemax.service.moviefeedback;

import com.bluebear.cinemax.dto.MovieFeedbackCommentDTO;
import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.MovieFeedbackRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.service.moviefeedbackcomment.MovieFeedbackCommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MovieFeedbackServiceImpl implements MovieFeedbackService {

    @Autowired
    private MovieFeedbackRepository feedbackRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieFeedbackCommentServiceImpl commentService;

    public MovieFeedbackDTO create(MovieFeedbackDTO dto) {
        MovieFeedback feedback = toEntity(dto);
        LocalDateTime now = LocalDateTime.now();
        feedback.setCreatedDate(now);
        return toDTO(feedbackRepository.save(feedback));
    }

    public MovieFeedbackDTO getById(Integer id) {
        return feedbackRepository.findById(id).map(this::toDTO).orElse(null);
    }

    public MovieFeedbackDTO update(Integer id, MovieFeedbackDTO dto) {
        return feedbackRepository.findById(id).map(existing -> {
            Optional<Customer> customerOpt = customerRepository.findById(dto.getCustomerId());
            Optional<Movie> movieOpt = movieRepository.findById(dto.getMovieId());
            existing.setCustomer(customerOpt.get());
            existing.setMovie(movieOpt.get());
            existing.setContent(dto.getContent());
            existing.setMovieRate(dto.getMovieRate());
            return toDTO(feedbackRepository.save(existing));
        }).orElse(null);
    }

    public boolean delete(Integer id) {
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Page<MovieFeedbackDTO> getAllByMovieIdSort(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return feedbackRepository.findByMovieOrderByCreatedDateDesc(movie, pageable)
                .map(this::toDTO);
    }

    public Page<MovieFeedbackDTO> getAllByMovieId(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return feedbackRepository.findByMovieOrderByIdDesc(movie, pageable)
                .map(this::toDTO);
    }

    public Page<MovieFeedbackDTO> getAllByMovieIdWithComments(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        Page<MovieFeedbackDTO> feedbackDTOS = feedbackRepository.findByMovieOrderByIdDesc(movie, pageable).map(this::toDTO);

        for (MovieFeedbackDTO feedbackDTO : feedbackDTOS) {
            List<MovieFeedbackCommentDTO> commentDTOS = commentService.getCommentsByFeedbackId(feedbackDTO.getId(), Pageable.unpaged()).toList();
            feedbackDTO.setComments(commentDTOS);
        }

        return feedbackDTOS;
    }

    public Page<MovieFeedbackDTO> getAllByMovieIdWithCommentCount(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        Page<MovieFeedbackDTO> movieFeedbackDTOS = feedbackRepository.findByMovieOrderByIdDesc(movie, pageable).map(this::toDTO);

        for (MovieFeedbackDTO movieFeedbackDTO : movieFeedbackDTOS) {
            movieFeedbackDTO.setTotalComments(commentService.countCommentByFeedbackId(movieFeedbackDTO.getId()));
        }

        return movieFeedbackDTOS;
    }

    public MovieFeedbackDTO getFeedBackWithCommentByFeedbackId(Integer feedbackId) {
        MovieFeedbackDTO feedbackDTO = getById(feedbackId);
        List<MovieFeedbackCommentDTO> commentDTOS = commentService.getCommentsByFeedbackId(feedbackId, Pageable.unpaged()).toList();
        feedbackDTO.setComments(commentDTOS);
        return feedbackDTO;
    }

    public MovieFeedbackDTO getFeedBackWithCommentCountByFeedbackId(Integer feedbackId) {
        MovieFeedbackDTO feedbackDTO = getById(feedbackId);
        feedbackDTO.setTotalComments(commentService.countCommentByFeedbackId(feedbackId));
        return feedbackDTO;
    }

    public int countDistinctMovieByFeedBack() {
        return (int) feedbackRepository.countMoviesWithFeedback();
    }

    public MovieFeedbackDTO toDTO(MovieFeedback feedback) {
        MovieFeedbackDTO dto = new MovieFeedbackDTO();
        dto.setId(feedback.getId());
        dto.setCustomerId(feedback.getCustomer().getId());
        dto.setCustomerName(feedback.getCustomer().getFullName());
        dto.setMovieId(feedback.getMovie().getMovieID());
        dto.setContent(feedback.getContent());
        dto.setMovieRate(feedback.getMovieRate());
        dto.setCreatedDate(feedback.getCreatedDate());
        return dto;
    }

    public MovieFeedback toEntity(MovieFeedbackDTO dto) {
        Optional<Customer> customerOpt = customerRepository.findById(dto.getCustomerId());
        Optional<Movie> movieOpt = movieRepository.findById(dto.getMovieId());

        if (customerOpt.isEmpty() || movieOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid customerId or movieId");
        }

        MovieFeedback feedback = new MovieFeedback();
        feedback.setId(dto.getId());
        feedback.setCustomer(customerOpt.get());
        feedback.setMovie(movieOpt.get());
        feedback.setContent(dto.getContent());
        feedback.setMovieRate(dto.getMovieRate());
        feedback.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : LocalDateTime.now());
        return feedback;
    }
}
