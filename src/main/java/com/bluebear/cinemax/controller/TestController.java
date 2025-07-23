package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.service.detailseat.DetailSeatService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.moviefeedbackcomment.MovieFeedbackCommentService;
import com.bluebear.cinemax.service.room.RoomService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.seat.SeatService;
import com.bluebear.cinemax.service.theater.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieFeedbackService movieFeedbackService;
    @Autowired
    private MovieFeedbackCommentService movieFeedbackCommentService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private DetailSeatService detailSeatService;

    @GetMapping("/movieToday")
    public Page<MovieDTO> getMovieToday() {
        return movieService.findMoviesByScheduleToday(LocalDateTime.now());
    }

    @GetMapping("/schduleByMovie/{movieId}")
    public Page<ScheduleDTO> getScheduleByMovie(@PathVariable int movieId) {
        return scheduleService.getScheduleByMovieIdAndDate(movieId, LocalDateTime.now());
    }

    @GetMapping("/movieByScheduleAndTheater/{theaterId}/{date}")
    public Page<MovieDTO> getMovieByScheduleAndTheater(@PathVariable int theaterId, @PathVariable int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +date);
        Page<MovieDTO> movieDTOS = movieService.findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime.now(), theaterId, "Single");
        return movieDTOS;
    }

    @GetMapping("/getMoviedetail/{movieId}/{day}/{theaterId}")
    public Page<ScheduleDTO> getMovieDetailMovieTheater(@PathVariable int movieId, @PathVariable int day, @PathVariable int theaterId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +day);
        return scheduleService.getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(movieId, theaterId, LocalDateTime.now(), "Single");
    }

    @GetMapping("/moviedetailfeedback/{movieId}/{feedbackPage}")
    public Page<MovieFeedbackDTO> getMovieFeedback(@PathVariable int movieId, @PathVariable int feedbackPage) {
        return movieFeedbackService.getAllByMovieId(movieId, PageRequest.of(feedbackPage, 10));
    }

    @GetMapping("/testgetAllMovies/{day}/{room}")
    public List<MovieDTO> getAllMovies(@PathVariable int day, @PathVariable String room) {
        return movieService.findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime.now().plusDays(day), 1, room).getContent();
    }

    @GetMapping("/testGetFeedbackCommentList/{movieId}")
    public List<MovieFeedbackDTO> getMovieFeedbackTestCommentList(@PathVariable int movieId) {
        return movieFeedbackService.getAllByMovieIdWithComments(movieId, Pageable.unpaged()).toList();
    }

    @GetMapping("/testGetFeedbackCommentCount/{movieId}")
    public List<MovieFeedbackDTO> getMovieFeedbackTestCommentCount(@PathVariable int movieId) {
        return movieFeedbackService.getAllByMovieIdWithCommentCount(movieId, Pageable.unpaged()).toList();
    }

    @GetMapping("/testInsertFeedback/{movieId}/{customerId}/{content}/{rate}")
    public List<MovieFeedbackDTO> insertMovieFeedbackTest(@PathVariable int movieId, @PathVariable int customerId, @PathVariable String content, @PathVariable Integer rate) {
        rate = null;
        MovieFeedbackDTO movieFeedbackDTO = MovieFeedbackDTO.builder().movieId(movieId).customerId(customerId).content(content).movieRate(rate).build();
        movieFeedbackService.create(movieFeedbackDTO);
        return movieFeedbackService.getAllByMovieIdWithComments(movieId, Pageable.unpaged()).toList();
    }

    @GetMapping("/testInsertComment/{feedbackId}/{authorId}/{repliedId}/{content}")
    public MovieFeedbackDTO insertComment(@PathVariable int feedbackId, @PathVariable int authorId, @PathVariable int repliedId, @PathVariable String content) {
        MovieFeedbackCommentDTO commentDTO = MovieFeedbackCommentDTO.builder().feedbackId(feedbackId).authorCustomerId(authorId).repliedToCustomerId(repliedId).content(content).build();
        movieFeedbackCommentService.createComment(commentDTO);
        return movieFeedbackService.getFeedBackWithCommentByFeedbackId(feedbackId);
    }

    @GetMapping("/testGetMovieByActor/{actorName}")
    public List<MovieDTO> getMovieByActor(@PathVariable String actorName) {
        return movieService.getMoviesByActor(actorName);
    }

    @GetMapping("/tesstuser/{customerId}/{movieID}")
    public boolean testUser(@PathVariable int customerId, @PathVariable int movieID) {
        return detailSeatService.hasCustomerWatched(customerId, movieID);
    }

}
