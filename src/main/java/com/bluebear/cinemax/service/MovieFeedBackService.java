package com.bluebear.cinemax.service;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.repository.MovieFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieFeedBackService {
    @Autowired
    private MovieFeedbackRepository movieFeedbackRepository;

    // Lấy tất cả feedbacks
    public List<MovieFeedback> getAllFeedbacks() {
        return movieFeedbackRepository.findAll();
    }

    // Lấy feedback theo ID
    public Optional<MovieFeedback> getFeedbackById(Long id) {
        return movieFeedbackRepository.findById(id);
    }

    // Lấy feedbacks theo MovieID
    public List<MovieFeedback> getFeedbacksByMovieId(String movieID) {
        return movieFeedbackRepository.findByMovieID(movieID);
    }

    // Lấy feedbacks theo CustomerID
    public List<MovieFeedback> getFeedbacksByCustomerId(String customerID) {
        return movieFeedbackRepository.findByCustomerID(customerID);
    }

    // Lưu feedback
    public MovieFeedback saveFeedback(MovieFeedback feedback) {
        return movieFeedbackRepository.save(feedback);
    }

    // Xóa feedback
    public void deleteFeedback(Long id) {
        movieFeedbackRepository.deleteById(id);
    }
}
