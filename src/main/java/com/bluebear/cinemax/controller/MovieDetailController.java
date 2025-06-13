package com.bluebear.cinemax.controller;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieGenre;
import com.bluebear.cinemax.service.MovieService;
import com.bluebear.cinemax.service.MovieGenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.bluebear.cinemax.service.MovieFeedBackService;
import java.util.List;

@Controller
@RequestMapping("/movie")
public class MovieDetailController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieGenreService movieGenreService;
    @Autowired
    private MovieFeedBackService movieFeedbackService;

    public MovieDetailController(MovieService movieService) {
        this.movieService = movieService;
    }

    // Quản lý MovieDTO
    @GetMapping("/index")
    public String getAllMovies(Model model) {
        model.addAttribute("movies", movieService.getAllMovies());
        return "common/movie-list";
    }
    @GetMapping("/detail/{id}")
    public String showMovieDetail(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + id)); // Xử lý khi không tìm thấy phim
        model.addAttribute("movie", movie);
        // Trả về tên view hiển thị chi tiết phim
        return "common/movie-detail";
    }
    @GetMapping("/new")
    public String showMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "common/movie-form";
    }

    @PostMapping("/save")
    public String saveMovie(@ModelAttribute("movie") Movie movie) {
        movieService.saveMovie(movie);
        return "redirect:/movie/index";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Movie movie = movieService.getMovieById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid movie Id:" + id));
        model.addAttribute("movie", movie);
        return "common/movie-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return "redirect:/movie/index";
    }

    @GetMapping("/search")
    public String searchMovies(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Movie> movies;

        if (keyword == null || keyword.trim().isEmpty()) {
            movies = movieService.getAllMovies();
            model.addAttribute("keyword", "");
        } else {
            movies = movieService.searchMoviesByName(keyword);
            model.addAttribute("keyword", keyword);
        }

        model.addAttribute("movies", movies);
        return "common/movie-list";
    }

    // Quản lý MovieGenre
    @GetMapping("/genres/index")
    public String getAllMovieGenres(Model model) {
        model.addAttribute("movieGenres", movieGenreService.getAllMovieGenres());
        return "common/movie";
    }

    @GetMapping("/genres/new")
    public String showMovieGenreForm(Model model) {
        model.addAttribute("movieGenre", new MovieGenre());
        return "common/movie";
    }

    @PostMapping("/genres/save")
    public String saveMovieGenre(@ModelAttribute("movieGenre") MovieGenre movieGenre) {
        movieGenreService.saveMovieGenre(movieGenre);
        return "redirect:/movie/genres/index";
    }

    @GetMapping("/genres/edit/{id}")
    public String showEditMovieGenreForm(@PathVariable int id, Model model) {
        MovieGenre movieGenre = movieGenreService.getMovieGenreById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid MovieGenre Id:" + id));
        model.addAttribute("movieGenre", movieGenre);
        return "common/movie-genre-form";
    }

    @GetMapping("/genres/delete/{id}")
    public String deleteMovieGenre(@PathVariable int id) {
        movieGenreService.deleteMovieGenre(id);
        return "redirect:/movie/genres/index";
    }

    @GetMapping("/genres/search")
    public String searchMovieGenres(@RequestParam(value = "movieId", required = false) Integer movieId,
                                    @RequestParam(value = "genreId", required = false) Integer genreId,
                                    Model model) {
        List<MovieGenre> movieGenres;

        if (movieId != null) {
            movieGenres = movieGenreService.findByMovieId(movieId);
        } else if (genreId != null) {
            movieGenres = movieGenreService.findByGenreId(genreId);
        } else {
            movieGenres = movieGenreService.getAllMovieGenres();
        }

        model.addAttribute("movieGenres", movieGenres);
        return "common/movie-genre-list";
    }
    @GetMapping("/feedbacks/index")
    public String getAllFeedbacks(Model model) {
        model.addAttribute("feedbacks", movieFeedbackService.getAllFeedbacks());
        return "common/movie-feedback-list";
    }

    @GetMapping("/feedbacks/new")
    public String showFeedbackForm(Model model) {
        model.addAttribute("movieFeedback", new MovieFeedback());
        return "common/movie-feedback-form";
    }

    @PostMapping("/feedbacks/save")
    public String saveFeedback(@ModelAttribute("movieFeedback") MovieFeedback feedback) {
        movieFeedbackService.saveFeedback(feedback);
        return "redirect:/movie/feedbacks/index";
    }

    @GetMapping("/feedbacks/edit/{id}")
    public String showEditFeedbackForm(@PathVariable Long id, Model model) {
        MovieFeedback feedback = movieFeedbackService.getFeedbackById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid feedback Id:" + id));
        model.addAttribute("movieFeedback", feedback);
        return "common/movie-feedback-form";
    }

    @GetMapping("/feedbacks/delete/{id}")
    public String deleteFeedback(@PathVariable Long id) {
        movieFeedbackService.deleteFeedback(id);
        return "redirect:/movie/feedbacks/index";
    }

    @GetMapping("/feedbacks/search")
    public String searchFeedbacks(@RequestParam(value = "movieId", required = false) String movieId,
                                  @RequestParam(value = "customerId", required = false) String customerId,
                                  Model model) {
        List<MovieFeedback> feedbacks;

        if (movieId != null && !movieId.isEmpty()) {
            feedbacks = movieFeedbackService.getFeedbacksByMovieId(movieId);
        } else if (customerId != null && !customerId.isEmpty()) {
            feedbacks = movieFeedbackService.getFeedbacksByCustomerId(customerId);
        } else {
            feedbacks = movieFeedbackService.getAllFeedbacks();
        }

        model.addAttribute("feedbacks", feedbacks);
        return "common/movie-feedback-list";
    }

}