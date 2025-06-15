package com.bluebear.cinemax.service.moviefeedback;

import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.MovieFeedbackRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieFeedbackServiceImpl implements MovieFeedbackService {

    @Autowired
    private MovieFeedbackRepository feedbackRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private MovieRepository movieRepository;

    // Create
    public MovieFeedbackDTO create(MovieFeedbackDTO dto) {
        MovieFeedback feedback = fromDTO(dto);
        feedback.setCreatedDate(new Date());
        return toDTO(feedbackRepository.save(feedback));
    }

    // Read (get by id)
    public MovieFeedbackDTO getById(Integer id) {
        return feedbackRepository.findById(id).map(this::toDTO).orElse(null);
    }

    // Update
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

    // Delete
    public boolean delete(Integer id) {
        if (feedbackRepository.existsById(id)) {
            feedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get feedback by movieId with pagination, sorted by CreatedDate desc
    public List<MovieFeedbackDTO> getAllByMovieIdSort(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return feedbackRepository.findByMovieOrderByCreatedDateDesc(movie, pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<MovieFeedbackDTO> getAllByMovieId(Integer movieId, Pageable pageable) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return feedbackRepository.findByMovie(movie, pageable)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Count feedbacks by movieId
    public int countFeedbackByMovieId(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return (int) feedbackRepository.countByMovie(movie);
    }

    public int countDistinctMovieByFeedBack() {
        return (int) feedbackRepository.countMoviesWithFeedback();
    }
    // --- Mapping methods ---

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

    public MovieFeedback fromDTO(MovieFeedbackDTO dto) {
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
        feedback.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : new Date());
        return feedback;
    }
}
