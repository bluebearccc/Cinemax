package com.bluebear.cinemax.controller;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieGenre;
import com.bluebear.cinemax.repository.MovieFeedbackRepository;
import com.bluebear.cinemax.service.MovieService;
import com.bluebear.cinemax.service.MovieGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bluebear.cinemax.service.MovieFeedBackService;
import java.util.List;
@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private MovieFeedbackRepository feedbackRepo;

    @PostMapping("/save")
    public String saveFeedback(@RequestParam String movieID,
                               @RequestParam String customerID,
                               @RequestParam String content,
                               @RequestParam int movieRate) {
        MovieFeedback fb = new MovieFeedback();
        fb.setMovieID(movieID);
        fb.setCustomerID(customerID);
        fb.setContent(content);
        fb.setMovieRate(movieRate);
        feedbackRepo.save(fb);

        return "redirect:/movie/detail/" + movieID;
    }
}
