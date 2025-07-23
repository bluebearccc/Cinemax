package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.service.detailseat.DetailSeatService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.moviefeedbackcomment.MovieFeedbackCommentService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
//@RestController
@RequestMapping("/customer/movie-detail")
public class MovieDetailController {

    @Autowired
    private MovieService movieService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private MovieFeedbackService movieFeedbackService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private MovieFeedbackCommentService commentService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private DetailSeatService detailSeatService;

    List<GenreDTO> genres;
    Page<TheaterDTO> theaters;
    Page<MovieDTO> top5movies;
    Page<ScheduleDTO> schedules;
    Map<Integer, Page<MovieFeedbackCommentDTO>> movieFeedbacks;
    Page<MovieFeedbackDTO> listFeedback;
    TheaterDTO currentTheater;
    String currentWebPage;

    @PostConstruct
    public void preInit() {
        genres = genreService.getAllGenres();
        top5movies = movieService.findTop5MoviesHighestRate();
        theaters = theaterService.getAllTheaters();
    }

    @GetMapping
    public String movieDetail(Model model, @RequestParam int movieId, HttpSession session) {
        boolean isViewed = false;
        movieFeedbacks = new LinkedHashMap<>();
        String roomType = "single";
        boolean isNearest = false;

        if (session.getAttribute("customer") != null) {
            CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
            isViewed = detailSeatService.hasCustomerWatched(customerDTO.getId(), movieId);
        }

        currentTheater = theaters.getContent().getFirst();
        MovieDTO movieDTO = movieService.findMovieByIdWithGenresAndActors(movieId);
        if (movieDTO != null) {
            listFeedback = movieFeedbackService.getAllByMovieIdWithCommentCount(movieId, PageRequest.of(0, Constant.FEEDBACK_PER_PAGE));
            schedules = scheduleService.getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(movieId, currentTheater.getTheaterID(), LocalDateTime.now(), roomType);
            for (ScheduleDTO scheduleDTO : schedules.getContent()) {
                scheduleService.calculateNumOfSeatLeft(scheduleDTO);
            }

            for (MovieFeedbackDTO movieFeedbackDTO : listFeedback.getContent()) {
                movieFeedbacks.put(movieFeedbackDTO.getId(), Page.empty());
            }

            currentWebPage = "movies";

            model.addAttribute("isViewed", isViewed);
            model.addAttribute("isNearest", isNearest);
            model.addAttribute("roomType", roomType);
            model.addAttribute("schedules", schedules);
            model.addAttribute("movieFeedbacks", movieFeedbacks);
            model.addAttribute("listFeedback", listFeedback);
            model.addAttribute("currentPage", listFeedback.getNumber());
            model.addAttribute("totalPage", listFeedback.getTotalPages());
            model.addAttribute("currentWebPage", currentWebPage);
            model.addAttribute("currentTheater", currentTheater);
            model.addAttribute("movie", movieDTO);
            model.addAttribute("top5movies", top5movies);
            model.addAttribute("theaters", theaters);
            model.addAttribute("genres", genres);
            return "customer/movie-detail";
        }

        return "common/error404";
    }

    @GetMapping("/loadSchedule")
    public String loadSchedule(Model model, @RequestParam(name = "movieId") int movieId, @RequestParam(name = "selectedIndex") int selectedIndex, @RequestParam(name = "theaterId") int theaterId, @RequestParam(name = "roomType") String roomType, @RequestParam(name = "isNearest") Boolean isNearest) {
        currentTheater = theaterService.getTheaterById(theaterId);
        LocalDateTime dateTime = LocalDateTime.now();
        if (selectedIndex > 0) {
            dateTime = LocalDateTime.now().plusDays(selectedIndex).toLocalDate().atStartOfDay();
        }
        schedules = scheduleService.getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(movieId, currentTheater.getTheaterID(), dateTime, roomType);
        for (ScheduleDTO scheduleDTO : schedules.getContent()) {
            scheduleService.calculateNumOfSeatLeft(scheduleDTO);
        }

        model.addAttribute("isNearest", isNearest);
        model.addAttribute("roomType", roomType);
        model.addAttribute("schedules", schedules);
        model.addAttribute("theaters", theaters);
        model.addAttribute("currentTheater", currentTheater);

        return "customer/fragments/movie-detail/book-detail :: book-detail";
    }

    @GetMapping("/loadFeedback")
    public String loadFeedback(Model model, @RequestParam int movieId, @RequestParam int currentPage) {
        boolean isViewed = false;
        Page<MovieFeedbackDTO> newFeedBacks = movieFeedbackService.getAllByMovieIdWithCommentCount(movieId, PageRequest.of(currentPage + 1, Constant.FEEDBACK_PER_PAGE));
        List<MovieFeedbackDTO> feedbackDTOS = new ArrayList<>();
        feedbackDTOS.addAll(listFeedback.getContent());
        feedbackDTOS.addAll(newFeedBacks.getContent());

        listFeedback = new PageImpl<>(feedbackDTOS, PageRequest.of(0, feedbackDTOS.size()), feedbackDTOS.size());

        for (MovieFeedbackDTO movieFeedbackDTO : newFeedBacks.getContent()) {
            movieFeedbacks.put(movieFeedbackDTO.getId(), Page.empty());
        }
        model.addAttribute("isViewed", isViewed);
        model.addAttribute("currentPage", newFeedBacks.getNumber());
        model.addAttribute("totalPage", newFeedBacks.getTotalPages());
        model.addAttribute("movieFeedbacks", movieFeedbacks);
        model.addAttribute("listFeedback", listFeedback);
        return "customer/fragments/movie-detail/feedback-detail :: feedback-detail";
    }

    @GetMapping("/loadComment")
    public String loadComment(Model model, @RequestParam int currentNumOfComment, @RequestParam int feedbackId) {
        boolean isViewed = false;
        currentNumOfComment = (int) Math.ceil((double) currentNumOfComment / Constant.COMMENT_PER_FEEDBACK);
        Page<MovieFeedbackCommentDTO> newListComment = commentService.getCommentsByFeedbackId(feedbackId, PageRequest.of(currentNumOfComment, Constant.FEEDBACK_PER_PAGE));
        MovieFeedbackDTO feedbackDTO = movieFeedbackService.getFeedBackWithCommentCountByFeedbackId(feedbackId);
        List<MovieFeedbackCommentDTO> feedbackDTOS = new ArrayList<>();
        feedbackDTOS.addAll(movieFeedbacks.get(feedbackId).getContent());
        feedbackDTOS.addAll(newListComment.getContent());
        if (feedbackDTOS.size() > 0) {
            movieFeedbacks.put(feedbackId, new PageImpl<>(feedbackDTOS, PageRequest.of(0, feedbackDTOS.size()), feedbackDTOS.size()));
        }
        model.addAttribute("isViewed", isViewed);
        model.addAttribute("feedback", feedbackDTO);
        model.addAttribute("movieFeedbacks", movieFeedbacks);
        return "customer/fragments/movie-detail/feedback-comment :: feedback-comment";
    }

    @PostMapping("/addFeedback")
    public String addFeedback(Model model, @RequestParam Integer movieId, @RequestParam Integer customerId, @RequestParam String content, @RequestParam(required = false) Integer rate, @RequestParam Integer currentPage, @RequestParam Integer totalPage) {
        boolean isViewed = false;
        MovieFeedbackDTO movieFeedbackDTO = MovieFeedbackDTO.builder().movieId(movieId).customerId(customerId).content(content).movieRate(rate).createdDate(LocalDateTime.now()).build();
        MovieFeedbackDTO createdFeedBack = movieFeedbackService.create(movieFeedbackDTO);
        listFeedback = movieFeedbackService.getAllByMovieIdWithCommentCount(movieId, PageRequest.of(0, Constant.FEEDBACK_PER_PAGE * (currentPage + 1)));
        movieFeedbacks.put(createdFeedBack.getId(), Page.empty());
        model.addAttribute("isViewed", isViewed);
        model.addAttribute("movieFeedbacks", movieFeedbacks);
        model.addAttribute("listFeedback", listFeedback);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPage", totalPage);
        return "customer/fragments/movie-detail/feedback-detail :: feedback-detail";
    }

    @PostMapping("/addComment")
    public String addComment(Model model, @RequestParam Integer feedbackId, @RequestParam Integer authorId, @RequestParam Integer repliedId, @RequestParam String content, @RequestParam int currentNumOfComment) {
        boolean isViewed = false;
        MovieFeedbackCommentDTO commentDTO = MovieFeedbackCommentDTO.builder().feedbackId(feedbackId).authorCustomerId(authorId).repliedToCustomerId(repliedId).content(content).createdDate(LocalDateTime.now()).build();
        commentService.createComment(commentDTO);

        currentNumOfComment = (int) Math.ceil((double) currentNumOfComment / Constant.COMMENT_PER_FEEDBACK);
        Page<MovieFeedbackCommentDTO> newListComment = commentService.getCommentsByFeedbackId(feedbackId, PageRequest.of(0, (Constant.FEEDBACK_PER_PAGE * (currentNumOfComment + 1) + 1)));
        MovieFeedbackDTO feedbackDTO = movieFeedbackService.getFeedBackWithCommentCountByFeedbackId(feedbackId);
        movieFeedbacks.replace(feedbackId, newListComment);
        model.addAttribute("isViewed", isViewed);
        model.addAttribute("feedback", feedbackDTO);
        model.addAttribute("movieFeedbacks", movieFeedbacks);
        return "customer/fragments/movie-detail/feedback-comment :: feedback-comment";
    }

}
