package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.service.ActorService;
import com.bluebear.cinemax.service.GenreService;
import com.bluebear.cinemax.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ActorService actorService;

    @Autowired
    private GenreService genreService;

    // ==================== BASIC MOVIE OPERATIONS ====================

    /**
     * Trang chủ - hiển thị tất cả phim
     */
    @GetMapping("")
    public String getAllMovies(Model model) {
        List<MovieDTO> movies = movieService.getAllActiveMovies();
        List<Genre> genres = genreService.getAllGenres();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("pageTitle", "Tất cả phim");

        return "admin/movies";
    }

    /**
     * Phim đang chiếu
     */
    @GetMapping("/now-showing")
    public String getNowShowingMovies(Model model) {
        List<MovieDTO> movies = movieService.getNowShowingMovies();
        List<Genre> genres = genreService.getAllGenres();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("pageTitle", "Phim đang chiếu");

        return "admin/movies";
    }

    /**
     * Phim sắp chiếu
     */
    @GetMapping("/upcoming")
    public String getUpcomingMovies(Model model) {
        List<MovieDTO> movies = movieService.getUpcomingMovies();
        List<Genre> genres = genreService.getAllGenres();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("pageTitle", "Phim sắp chiếu");

        return "admin/movies";
    }

    /**
     * Chi tiết phim
     */
    @GetMapping("/{id}")
    public String getMovieDetail(@PathVariable Integer id, Model model) {
        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                model.addAttribute("error", "Không tìm thấy phim");
                return "error/404";
            }

            List<ActorDTO> actors = actorService.getActorsByMovie(id);
            List<Genre> genres = genreService.getGenresByMovie(id);
            List<MovieDTO> relatedMovies = movieService.getRelatedMovies(id.longValue());

            model.addAttribute("movie", movie);
            model.addAttribute("actors", actors);
            model.addAttribute("genres", genres);
            model.addAttribute("relatedMovies", relatedMovies);
            model.addAttribute("pageTitle", movie.getMovieName());

            return "admin/detail";
        } catch (Exception e) {
            System.err.println("Error loading movie detail: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin phim");
            return "error/500";
        }
    }

    // ==================== SEARCH AND FILTER ====================

    /**
     * Tìm kiếm phim - Lọc theo tên phim, thể loại và status
     */
    @GetMapping("/search")
    public String searchMovies(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer genreId,
                               @RequestParam(required = false) String status,
                               Model model) {
        try {
            List<MovieDTO> movies;
            String pageTitle = "Kết quả tìm kiếm";
            StringBuilder titleBuilder = new StringBuilder();

            // Bước 1: Lọc theo Status
            if (status != null && !status.trim().isEmpty()) {
                if ("Active".equals(status)) {
                    movies = movieService.getAllActiveMovies();
                    titleBuilder.append("Phim Active");
                } else if ("Removed".equals(status)) {
                    movies = movieService.getAllMovies().stream()
                            .filter(movie -> "Removed".equals(movie.getStatus()))
                            .collect(Collectors.toList());
                    titleBuilder.append("Phim Removed");
                } else {
                    movies = movieService.getAllMovies();
                    titleBuilder.append("Tất cả phim");
                }
            } else {
                movies = movieService.getAllMovies();
                titleBuilder.append("Tất cả phim");
            }

            // Bước 2: Lọc theo tên phim (keyword)
            if (keyword != null && !keyword.trim().isEmpty()) {
                movies = movies.stream()
                        .filter(movie -> movie.getMovieName().toLowerCase()
                                .contains(keyword.toLowerCase()))
                        .collect(Collectors.toList());

                if (titleBuilder.length() > 0) titleBuilder.append(" | ");
                titleBuilder.append("Tìm kiếm: \"").append(keyword).append("\"");
            }

            // Bước 3: Lọc theo thể loại
            if (genreId != null) {
                movies = movies.stream()
                        .filter(movie -> movie.getGenres() != null &&
                                movie.getGenres().stream().anyMatch(genre -> {
                                    Genre g = genreService.getGenreById(genreId);
                                    return g != null && genre.equals(g.getGenreName());
                                }))
                        .collect(Collectors.toList());

                Genre genre = genreService.getGenreById(genreId);
                if (genre != null) {
                    if (titleBuilder.length() > 0) titleBuilder.append(" | ");
                    titleBuilder.append("Thể loại: ").append(genre.getGenreName());
                }
            }

            // Cập nhật page title
            if (titleBuilder.length() > 0) {
                pageTitle = titleBuilder.toString();
            }

            List<Genre> allGenres = genreService.getAllGenres();

            model.addAttribute("movies", movies);
            model.addAttribute("genres", allGenres);
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("resultCount", movies.size());

            return "admin/movies";
        } catch (Exception e) {
            System.err.println("Error in search: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm");
            return "admin/movies";
        }
    }

    /**
     * Top phim theo rating
     */
    @GetMapping("/top-rated")
    public String getTopRatedMovies(Model model) {
        List<MovieDTO> movies = movieService.getTopRatedMovies();
        List<Genre> genres = genreService.getAllGenres();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("pageTitle", "Phim được đánh giá cao");

        return "admin/movies";
    }

    /**
     * Theo thể loại
     */
    @GetMapping("/genre/{genreId}")
    public String getMoviesByGenre(@PathVariable Integer genreId, Model model) {
        try {
            Genre genre = genreService.getGenreById(genreId);
            if (genre == null) {
                model.addAttribute("error", "Không tìm thấy thể loại");
                return "error/404";
            }

            List<MovieDTO> movies = movieService.getMoviesByGenre(genreId);
            List<Genre> allGenres = genreService.getAllGenres();

            model.addAttribute("movies", movies);
            model.addAttribute("genres", allGenres);
            model.addAttribute("selectedGenre", genre);
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("pageTitle", "Thể loại: " + genre.getGenreName());

            return "admin/movies";
        } catch (Exception e) {
            System.err.println("Error loading movies by genre: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải phim theo thể loại");
            return "admin/movies";
        }
    }

    // ==================== MOVIE EDIT FUNCTIONALITY ====================

    /**
     * Hiển thị form chỉnh sửa phim - FIXED
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== SHOW EDIT FORM ===");
        System.out.println("Movie ID: " + id);

        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                System.out.println("Movie not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            // Lấy tất cả genres
            List<Genre> allGenres = genreService.getAllGenres();
            System.out.println("Total genres available: " + allGenres.size());

            // Lấy các genre IDs hiện tại của phim - FIXED
            List<Integer> movieGenreIds = genreService.getGenresByMovie(id).stream()
                    .map(Genre::getGenreID) // Fixed: use getGenreID() instead of getGenreId()
                    .collect(Collectors.toList());
            System.out.println("Current movie genres: " + movieGenreIds);

            model.addAttribute("movie", movie);
            model.addAttribute("allGenres", allGenres);
            model.addAttribute("movieGenreIds", movieGenreIds);
            model.addAttribute("pageTitle", "Chỉnh sửa phim - " + movie.getMovieName());

            System.out.println("Successfully loaded edit form for movie: " + movie.getMovieName());
            return "admin/edit";

        } catch (Exception e) {
            System.err.println("Error loading edit form: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải form chỉnh sửa");
            return "redirect:/movies";
        }
    }

    /**
     * Xử lý cập nhật phim - FIXED VERSION
     */
    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable Integer id,
                              @RequestParam Map<String, String> allParams,
                              @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {

        System.out.println("=========================");
        System.out.println("=== UPDATE MOVIE START ===");
        System.out.println("=========================");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Movie ID: " + id);
        System.out.println("Content Type: " + request.getContentType());

        // Log all parameters
        System.out.println("--- ALL FORM PARAMETERS ---");
        allParams.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("Selected Genre IDs: " + genreIds);
        System.out.println("=========================");

        try {
            // Kiểm tra phim có tồn tại không
            MovieDTO existingMovie = movieService.getMovieById(id);
            if (existingMovie == null) {
                System.out.println("ERROR: Movie not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim để cập nhật");
                return "redirect:/movies";
            }

            System.out.println("Found existing movie: " + existingMovie.getMovieName());

            // Lấy và validate dữ liệu từ form
            String movieName = allParams.get("movieName");
            String description = allParams.get("description");
            String image = allParams.get("image");
            String banner = allParams.get("banner");
            String studio = allParams.get("studio");
            String trailer = allParams.get("trailer");
            String startDateStr = allParams.get("startDate");
            String endDateStr = allParams.get("endDate");
            String status = allParams.get("status");

            // Validate tên phim
            if (movieName == null || movieName.trim().isEmpty()) {
                System.out.println("ERROR: Movie name is empty");
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/movies/" + id + "/edit";
            }

            // Validate và parse duration
            Integer duration;
            try {
                String durationStr = allParams.get("duration");
                if (durationStr == null || durationStr.trim().isEmpty()) {
                    throw new NumberFormatException("Duration is empty");
                }
                duration = Integer.parseInt(durationStr.trim());
                if (duration <= 0 || duration > 500) {
                    System.out.println("ERROR: Invalid duration: " + duration);
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                    return "redirect:/movies/" + id + "/edit";
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Cannot parse duration: " + allParams.get("duration"));
                redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                return "redirect:/movies/" + id + "/edit";
            }

            // Validate và parse movie rate (optional) - FIXED VERSION
            Double movieRate = null; // Changed to Double to match entity
            String movieRateStr = allParams.get("movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());

                    System.out.println("Controller: Parsed rating = " + movieRate);

                    if (movieRate < 0.0 || movieRate > 5.0) {
                        System.out.println("ERROR: Invalid movie rate: " + movieRate);
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/movies/" + id + "/edit";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Cannot parse movie rate: " + movieRateStr);
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/movies/" + id + "/edit";
                }
            }

            // Validate và parse dates - Convert to LocalDateTime
            LocalDate startDate, endDate;
            try {
                if (startDateStr == null || startDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("Start date is empty", "", 0);
                }
                if (endDateStr == null || endDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("End date is empty", "", 0);
                }

                startDate = LocalDate.parse(startDateStr.trim());
                endDate = LocalDate.parse(endDateStr.trim());

                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    System.out.println("ERROR: Invalid date range - Start: " + startDate + ", End: " + endDate);
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/movies/" + id + "/edit";
                }
            } catch (DateTimeParseException e) {
                System.out.println("ERROR: Cannot parse dates - Start: " + startDateStr + ", End: " + endDateStr);
                redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ (yyyy-MM-dd)");
                return "redirect:/movies/" + id + "/edit";
            }

            // Tạo Movie object để cập nhật - FIXED
            Movie movieToUpdate = new Movie();
            movieToUpdate.setMovieID(id); // Fixed: use setMovieID
            movieToUpdate.setMovieName(movieName.trim());
            movieToUpdate.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
            movieToUpdate.setImage(image != null && !image.trim().isEmpty() ? image.trim() : null);
            movieToUpdate.setBanner(banner != null && !banner.trim().isEmpty() ? banner.trim() : null);
            movieToUpdate.setStudio(studio != null && !studio.trim().isEmpty() ? studio.trim() : null);
            movieToUpdate.setDuration(duration);
            movieToUpdate.setTrailer(trailer != null && !trailer.trim().isEmpty() ? trailer.trim() : null);

            // Set rating - use Double instead of BigDecimal
            movieToUpdate.setMovieRate(movieRate);
            System.out.println("Controller: Setting movieToUpdate rating = " + movieToUpdate.getMovieRate());

            // Convert LocalDate to LocalDateTime for entity
            movieToUpdate.setStartDate(startDate.atStartOfDay()); // Convert to LocalDateTime
            movieToUpdate.setEndDate(endDate.atTime(23, 59, 59)); // Convert to LocalDateTime

            // Set status - FIXED to use proper enum
            if ("Active".equals(status)) {
                movieToUpdate.setStatus(Movie_Status.Active);
            } else {
                movieToUpdate.setStatus(Movie_Status.Removed);
            }

            System.out.println("=== MOVIE UPDATE DATA ===");
            System.out.println("Old name: " + existingMovie.getMovieName());
            System.out.println("New name: " + movieToUpdate.getMovieName());
            System.out.println("Old rating: " + existingMovie.getMovieRate());
            System.out.println("New rating: " + movieToUpdate.getMovieRate());
            System.out.println("New duration: " + movieToUpdate.getDuration());
            System.out.println("New start date: " + movieToUpdate.getStartDate());
            System.out.println("New end date: " + movieToUpdate.getEndDate());
            System.out.println("New status: " + movieToUpdate.getStatus());
            System.out.println("New genres: " + genreIds);

            // Debug rating trước update
            System.out.println("=== CONTROLLER: BEFORE UPDATE ===");
            movieService.debugMovieRating(id);

            // Thực hiện cập nhật
            System.out.println("Calling movieService.updateMovieComplete...");
            boolean updateSuccess = movieService.updateMovieComplete(id, movieToUpdate, genreIds);

            // Debug rating sau update
            System.out.println("=== CONTROLLER: AFTER UPDATE ===");
            movieService.debugMovieRating(id);

            if (updateSuccess) {
                System.out.println("SUCCESS: Movie updated successfully!");

                // Verify update by getting updated movie
                MovieDTO updatedMovie = movieService.getMovieById(id);
                if (updatedMovie != null) {
                    System.out.println("Verified - Updated movie name: " + updatedMovie.getMovieName());
                    System.out.println("Verified - Updated duration: " + updatedMovie.getDuration());
                    System.out.println("Verified - Updated rating: " + updatedMovie.getMovieRate());
                }

                redirectAttributes.addFlashAttribute("success",
                        "Cập nhật phim '" + movieName + "' thành công!");
                return "redirect:/movies/" + id;

            } else {
                System.out.println("ERROR: Movie update failed - service returned false");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật phim");
                return "redirect:/movies/" + id + "/edit";
            }

        } catch (IllegalArgumentException e) {
            System.err.println("VALIDATION ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/movies/" + id + "/edit";

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in updateMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/movies/" + id + "/edit";
        }
    }

    // ==================== END DATE EDIT FUNCTIONALITY ====================

    /**
     * Hiển thị form sửa ngày hết hạn chiếu
     */
    @GetMapping("/{id}/edit-enddate")
    public String showEditEndDateForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            model.addAttribute("movie", movie);
            model.addAttribute("pageTitle", "Sửa ngày hết hạn chiếu - " + movie.getMovieName());

            return "admin/edit-enddate";
        } catch (Exception e) {
            System.err.println("Error loading edit end date form: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải form");
            return "redirect:/movies";
        }
    }

    /**
     * Xử lý cập nhật ngày hết hạn chiếu
     */
    @PostMapping("/{id}/edit-enddate")
    public String updateEndDate(@PathVariable Integer id,
                                @RequestParam("endDate") String endDateStr,
                                RedirectAttributes redirectAttributes) {
        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            LocalDate endDate = LocalDate.parse(endDateStr);
            LocalDate startDate = movie.getStartDate();

            if (startDate != null && endDate.isBefore(startDate)) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc không thể trước ngày bắt đầu");
                return "redirect:/movies/" + id + "/edit-enddate";
            }

            boolean success = movieService.updateEndDate(id, endDate);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật ngày hết hạn chiếu thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật");
            }

        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ");
            return "redirect:/movies/" + id + "/edit-enddate";
        } catch (Exception e) {
            System.err.println("Error updating end date: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi hệ thống xảy ra");
            return "redirect:/movies/" + id + "/edit-enddate";
        }

        return "redirect:/movies/" + id;
    }

    // ==================== DEBUG AND API ENDPOINTS ====================

    /**
     * API endpoint để kiểm tra thông tin phim và genres - FIXED
     */
    @GetMapping("/{id}/debug")
    @ResponseBody
    public Map<String, Object> debugMovieInfo(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        try {
            MovieDTO movie = movieService.getMovieById(id);
            List<Genre> allGenres = genreService.getAllGenres();
            List<Genre> movieGenres = genreService.getGenresByMovie(id);
            List<Integer> movieGenreIds = movieGenres.stream()
                    .map(Genre::getGenreID) // Fixed: use getGenreID()
                    .collect(Collectors.toList());

            result.put("success", true);
            result.put("movie", movie);
            result.put("allGenres", allGenres);
            result.put("movieGenres", movieGenres);
            result.put("movieGenreIds", movieGenreIds);

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * API endpoint để test form submission
     */
    @PostMapping("/{id}/test")
    @ResponseBody
    public Map<String, Object> testFormSubmission(@PathVariable Integer id,
                                                  @RequestParam Map<String, String> allParams,
                                                  @RequestParam(value = "genreIds", required = false) List<Integer> genreIds) {
        Map<String, Object> result = new HashMap<>();

        try {
            result.put("success", true);
            result.put("movieId", id);
            result.put("totalParams", allParams.size());
            result.put("allParameters", allParams);
            result.put("genreIds", genreIds);
            result.put("message", "Form data received successfully");
            result.put("timestamp", java.time.LocalDateTime.now());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        try {
            long movieCount = movieService.countAllMovies();
            long genreCount = genreService.getAllGenres().size();

            result.put("status", "OK");
            result.put("movieCount", movieCount);
            result.put("genreCount", genreCount);
            result.put("timestamp", java.time.LocalDateTime.now());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * API endpoint để debug rating issues - NEW
     */
    @GetMapping("/{id}/debug-rating")
    @ResponseBody
    public Map<String, Object> debugRating(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                result.put("success", false);
                result.put("error", "Movie not found");
                return result;
            }

            // Debug rating trong service
            movieService.debugMovieRating(id);

            result.put("success", true);
            result.put("movieId", id);
            result.put("movieName", movie.getMovieName());
            result.put("currentRating", movie.getMovieRate());
            result.put("ratingClass", movie.getMovieRate() != null ? movie.getMovieRate().getClass().getSimpleName() : "null");
            result.put("timestamp", java.time.LocalDateTime.now());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * API endpoint để test rating update - NEW
     */
    @PostMapping("/{id}/test-rating")
    @ResponseBody
    public Map<String, Object> testRatingUpdate(@PathVariable Integer id,
                                                @RequestParam("rating") String ratingStr) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== TEST RATING UPDATE ===");
            System.out.println("Movie ID: " + id);
            System.out.println("Input rating string: '" + ratingStr + "'");

            // Parse rating như trong controller - use Double instead of BigDecimal
            Double testRating = null;
            if (ratingStr != null && !ratingStr.trim().isEmpty()) {
                testRating = Double.parseDouble(ratingStr.trim());
            }

            System.out.println("Parsed rating: " + testRating);

            // Get current movie
            MovieDTO currentMovie = movieService.getMovieById(id);
            if (currentMovie == null) {
                result.put("success", false);
                result.put("error", "Movie not found");
                return result;
            }

            result.put("success", true);
            result.put("movieId", id);
            result.put("inputRatingString", ratingStr);
            result.put("parsedRating", testRating);
            result.put("currentMovieRating", currentMovie.getMovieRate());
            result.put("ratingWillChange", testRating != null && !testRating.equals(currentMovie.getMovieRate()));
            result.put("timestamp", java.time.LocalDateTime.now());

            System.out.println("Test completed successfully");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // ==================== ADD MOVIE FUNCTIONALITY - FIXED ====================

    /**
     * Hiển thị form thêm phim mới
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        try {
            List<Genre> allGenres = genreService.getAllGenres();
            List<ActorDTO> allActors = actorService.getAllActors();

            model.addAttribute("allGenres", allGenres);
            model.addAttribute("allActors", allActors);
            model.addAttribute("pageTitle", "Thêm phim mới");

            return "admin/add-movie";
        } catch (Exception e) {
            System.err.println("Error loading add movie form: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải form thêm phim");
            return "redirect:/movies";
        }
    }

    /**
     * Xử lý thêm phim mới - FIXED VERSION
     */
    @PostMapping("/add")
    public String addMovie(@RequestParam Map<String, String> allParams,
                           @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                           @RequestParam(value = "actorIds", required = false) List<Integer> actorIds,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {

        System.out.println("=========================");
        System.out.println("=== ADD NEW MOVIE START ===");
        System.out.println("=========================");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Content Type: " + request.getContentType());

        // Log all parameters
        System.out.println("--- ALL FORM PARAMETERS ---");
        allParams.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("Selected Genre IDs: " + genreIds);
        System.out.println("Selected Actor IDs: " + actorIds);
        System.out.println("=========================");

        try {
            // Lấy dữ liệu từ form với NULL handling
            String movieName = getParameterOrNull(allParams, "movieName");
            String description = getParameterOrNull(allParams, "description");
            String image = getParameterOrNull(allParams, "image");
            String banner = getParameterOrNull(allParams, "banner");
            String studio = getParameterOrNull(allParams, "studio");
            String trailer = getParameterOrNull(allParams, "trailer");
            String startDateStr = getParameterOrNull(allParams, "startDate");
            String endDateStr = getParameterOrNull(allParams, "endDate");
            String status = getParameterOrNull(allParams, "status");

            // Validate tên phim - CHỈ CÓ TÊN PHIM LÀ BẮT BUỘC
            if (movieName == null || movieName.trim().isEmpty()) {
                System.out.println("ERROR: Movie name is empty");
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/movies/add";
            }

            // Kiểm tra tên phim trùng lặp
            if (movieService.isMovieNameExists(movieName.trim(), null)) {
                System.out.println("ERROR: Movie name already exists: " + movieName);
                redirectAttributes.addFlashAttribute("error", "Tên phim đã tồn tại");
                return "redirect:/movies/add";
            }

            // Parse duration - CHO PHÉP NULL
            Integer duration = null;
            String durationStr = getParameterOrNull(allParams, "duration");
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr.trim());
                    if (duration <= 0 || duration > 500) {
                        System.out.println("ERROR: Invalid duration: " + duration);
                        redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                        return "redirect:/movies/add";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Cannot parse duration: " + durationStr);
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                    return "redirect:/movies/add";
                }
            }

            // Parse movie rate - CHO PHÉP NULL - FIXED to use Double
            Double movieRate = null;
            String movieRateStr = getParameterOrNull(allParams, "movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());

                    System.out.println("Controller: Parsed rating = " + movieRate);

                    if (movieRate < 0.0 || movieRate > 5.0) {
                        System.out.println("ERROR: Invalid movie rate: " + movieRate);
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/movies/add";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Cannot parse movie rate: " + movieRateStr);
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/movies/add";
                }
            }

            // Parse dates - CHO PHÉP NULL
            LocalDate startDate = null;
            LocalDate endDate = null;

            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    startDate = LocalDate.parse(startDateStr.trim());
                } catch (DateTimeParseException e) {
                    System.out.println("ERROR: Cannot parse start date: " + startDateStr);
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày bắt đầu không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/movies/add";
                }
            }

            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    endDate = LocalDate.parse(endDateStr.trim());
                } catch (DateTimeParseException e) {
                    System.out.println("ERROR: Cannot parse end date: " + endDateStr);
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày kết thúc không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/movies/add";
                }
            }

            // Validate date range nếu cả hai đều không null
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    System.out.println("ERROR: Invalid date range - Start: " + startDate + ", End: " + endDate);
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/movies/add";
                }
            }

            // Validate genres - CHO PHÉP EMPTY
            if (genreIds != null && !genreIds.isEmpty()) {
                if (genreIds.size() > 5) {
                    System.out.println("ERROR: Too many genres selected: " + genreIds.size());
                    redirectAttributes.addFlashAttribute("error", "Chỉ được chọn tối đa 5 thể loại");
                    return "redirect:/movies/add";
                }

                // Validate genres exist
                for (Integer genreId : genreIds) {
                    if (!genreService.existsById(genreId)) {
                        System.out.println("ERROR: Genre not found: " + genreId);
                        redirectAttributes.addFlashAttribute("error", "Thể loại không tồn tại");
                        return "redirect:/movies/add";
                    }
                }
            }

            // Validate actors - CHO PHÉP EMPTY
            if (actorIds != null && !actorIds.isEmpty()) {
                for (Integer actorId : actorIds) {
                    if (actorService.getActorById(actorId) == null) {
                        System.out.println("ERROR: Actor not found: " + actorId);
                        redirectAttributes.addFlashAttribute("error", "Diễn viên không tồn tại");
                        return "redirect:/movies/add";
                    }
                }
            }

            // Tạo Movie object - FIXED
            Movie newMovie = new Movie();
            newMovie.setMovieName(movieName.trim());
            newMovie.setDescription(description); // có thể null
            newMovie.setImage(image); // có thể null
            newMovie.setBanner(banner); // có thể null
            newMovie.setStudio(studio); // có thể null
            newMovie.setDuration(duration); // có thể null - sẽ được set default
            newMovie.setTrailer(trailer); // có thể null
            newMovie.setMovieRate(movieRate); // Double type - có thể null

            // Convert LocalDate to LocalDateTime for entity
            if (startDate != null) {
                newMovie.setStartDate(startDate.atStartOfDay());
            }
            if (endDate != null) {
                newMovie.setEndDate(endDate.atTime(23, 59, 59));
            }

            // Set status - FIXED to use proper enum
            if ("Active".equals(status)) {
                newMovie.setStatus(Movie_Status.Active);
            } else if ("Removed".equals(status)) {
                newMovie.setStatus(Movie_Status.Removed);
            } else {
                newMovie.setStatus(null); // sẽ được set default
            }

            System.out.println("=== NEW MOVIE DATA ===");
            System.out.println("Name: " + newMovie.getMovieName());
            System.out.println("Duration: " + newMovie.getDuration());
            System.out.println("Rating: " + newMovie.getMovieRate());
            System.out.println("Start date: " + newMovie.getStartDate());
            System.out.println("End date: " + newMovie.getEndDate());
            System.out.println("Status: " + newMovie.getStatus());
            System.out.println("Genres: " + genreIds);
            System.out.println("Actors: " + actorIds);

            // Thực hiện thêm phim mới
            System.out.println("Calling movieService.addMovie...");
            MovieDTO addedMovie = movieService
                    .addMovie(newMovie, genreIds, actorIds);

            if (addedMovie != null) {
                System.out.println("SUCCESS: Movie added successfully with ID: " + addedMovie.getMovieId());
                redirectAttributes.addFlashAttribute("success",
                        "Thêm phim '" + movieName + "' thành công! Các thông tin chưa nhập sẽ được set giá trị mặc định.");
                return "redirect:/movies/" + addedMovie.getMovieId();
            } else {
                System.out.println("ERROR: Movie add failed - service returned null");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm phim");
                return "redirect:/movies/add";
            }

        } catch (IllegalArgumentException e) {
            System.err.println("VALIDATION ERROR: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/movies/add";

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in addMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/movies/add";
        }
    }

    /**
     * Helper method để lấy parameter hoặc null nếu empty
     */
    private String getParameterOrNull(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    // ==================== DELETE FUNCTIONALITY - FIXED ====================

    /**
     * Xóa vĩnh viễn phim khỏi database (chỉ cho phim có status "Removed")
     */
    @PostMapping("/{id}/hard-delete")
    public String hardDeleteMovie(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        System.out.println("=== HARD DELETE MOVIE ===");
        System.out.println("Movie ID: " + id);

        try {
            // Kiểm tra phim có tồn tại không
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                System.out.println("ERROR: Movie not found with ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            // Kiểm tra status phải là "Removed"
            if (!"Removed".equals(movie.getStatus())) {
                System.out.println("ERROR: Movie status is not 'Removed': " + movie.getStatus());
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa vĩnh viễn những phim có trạng thái 'Removed'");
                return "redirect:/movies/" + id;
            }

            String movieName = movie.getMovieName();
            System.out.println("Attempting to hard delete movie: " + movieName);

            // Thực hiện xóa vĩnh viễn
            boolean deleteSuccess = movieService.hardDeleteMovieComplete(id);

            if (deleteSuccess) {
                System.out.println("SUCCESS: Movie hard deleted successfully!");
                redirectAttributes.addFlashAttribute("success",
                        "Đã xóa vĩnh viễn phim '" + movieName + "' khỏi hệ thống!");
                return "redirect:/movies";
            } else {
                System.out.println("ERROR: Hard delete failed");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa phim");
                return "redirect:/movies/" + id;
            }

        } catch (Exception e) {
            System.err.println("ERROR in hardDeleteMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/movies/" + id;
        }
    }

}