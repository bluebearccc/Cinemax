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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // ==================== CÁC THAO TÁC CƠ BẢN VỚI PHIM ====================

    /**
     * Trang chủ - hiển thị tất cả phim
     */
    @GetMapping("")
    public String getAllMovies(Model model) {
        List<MovieDTO> movies = movieService.getAllMovies();
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
            System.err.println("Lỗi khi tải chi tiết phim: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin phim");
            return "error/500";
        }
    }

    // ==================== TÌM KIẾM VÀ LỌC ====================

    /**
     * Tìm kiếm phim - Lọc theo tên phim, thể loại và trạng thái
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

            // Bước 1: Lọc theo Trạng thái
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

            // Bước 2: Lọc theo tên phim (từ khóa)
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

            // Cập nhật tiêu đề trang
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
            System.err.println("Lỗi trong tìm kiếm: " + e.getMessage());
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
            System.err.println("Lỗi khi tải phim theo thể loại: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải phim theo thể loại");
            return "admin/movies";
        }
    }

    // ==================== CHỨC NĂNG CHỈNH SỬA PHIM ====================

    /**
     * Hiển thị form chỉnh sửa phim - ĐÃ SỬA
     */
    @GetMapping("/{id}/edit")
    @Transactional
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        System.out.println("=== HIỂN THỊ FORM CHỈNH SỬA ===");
        System.out.println("ID Phim: " + id);

        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                System.out.println("Không tìm thấy phim với ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            // Lấy tất cả thể loại
            List<Genre> allGenres = genreService.getAllGenres();
            System.out.println("Tổng số thể loại có sẵn: " + allGenres.size());

            // Lấy các ID thể loại hiện tại của phim - ĐÃ SỬA
            List<Integer> movieGenreIds = genreService.getGenresByMovie(id).stream()
                    .map(Genre::getGenreID) // Đã sửa: sử dụng getGenreID() thay vì getGenreId()
                    .collect(Collectors.toList());
            System.out.println("Thể loại hiện tại của phim: " + movieGenreIds);

            model.addAttribute("movie", movie);
            model.addAttribute("allGenres", allGenres);
            model.addAttribute("movieGenreIds", movieGenreIds);
            model.addAttribute("pageTitle", "Chỉnh sửa phim - " + movie.getMovieName());

            System.out.println("Đã tải thành công form chỉnh sửa cho phim: " + movie.getMovieName());
            return "admin/edit";

        } catch (Exception e) {
            System.err.println("Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải form chỉnh sửa");
            return "redirect:/movies";
        }
    }

    /**
     * Xử lý cập nhật phim - PHIÊN BẢN ĐÃ SỬA
     */
    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable Integer id,
                              @RequestParam Map<String, String> allParams,
                              @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {

        System.out.println("=========================");
        System.out.println("=== BẮT ĐẦU CẬP NHẬT PHIM ===");
        System.out.println("=========================");
        System.out.println("URL yêu cầu: " + request.getRequestURL());
        System.out.println("URI yêu cầu: " + request.getRequestURI());
        System.out.println("Phương thức yêu cầu: " + request.getMethod());
        System.out.println("ID phim: " + id);
        System.out.println("Loại nội dung: " + request.getContentType());

        // Ghi log tất cả tham số
        System.out.println("--- TẤT CẢ THAM SỐ FORM ---");
        allParams.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("ID thể loại được chọn: " + genreIds);
        System.out.println("=========================");

        try {
            // Kiểm tra phim có tồn tại không
            MovieDTO existingMovie = movieService.getMovieById(id);
            if (existingMovie == null) {
                System.out.println("LỖI: Không tìm thấy phim với ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim để cập nhật");
                return "redirect:/movies";
            }

            System.out.println("Đã tìm thấy phim hiện tại: " + existingMovie.getMovieName());

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
                System.out.println("LỖI: Tên phim trống");
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/movies/" + id + "/edit";
            }

            // Validate và parse thời lượng
            Integer duration;
            try {
                String durationStr = allParams.get("duration");
                if (durationStr == null || durationStr.trim().isEmpty()) {
                    throw new NumberFormatException("Thời lượng trống");
                }
                duration = Integer.parseInt(durationStr.trim());
                if (duration <= 0 || duration > 500) {
                    System.out.println("LỖI: Thời lượng không hợp lệ: " + duration);
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                    return "redirect:/movies/" + id + "/edit";
                }
            } catch (NumberFormatException e) {
                System.out.println("LỖI: Không thể parse thời lượng: " + allParams.get("duration"));
                redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                return "redirect:/movies/" + id + "/edit";
            }

            // Validate và parse đánh giá phim (tùy chọn) - PHIÊN BẢN ĐÃ SỬA SỬ DỤNG DOUBLE
            Double movieRate = null; // Đổi thành Double để khớp với entity
            String movieRateStr = allParams.get("movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());

                    System.out.println("Controller: Đánh giá đã parse = " + movieRate);

                    if (movieRate < 0.0 || movieRate > 5.0) {
                        System.out.println("LỖI: Đánh giá phim không hợp lệ: " + movieRate);
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/movies/" + id + "/edit";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("LỖI: Không thể parse đánh giá phim: " + movieRateStr);
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/movies/" + id + "/edit";
                }
            }

            // Validate và parse ngày - Chuyển đổi thành LocalDateTime
            LocalDateTime startDate, endDate;
            try {
                if (startDateStr == null || startDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("Ngày bắt đầu trống", "", 0);
                }
                if (endDateStr == null || endDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("Ngày kết thúc trống", "", 0);
                }

                // Parse ngày và chuyển thành LocalDateTime
                startDate = LocalDateTime.parse(startDateStr.trim() + "T00:00:00");
                endDate = LocalDateTime.parse(endDateStr.trim() + "T23:59:59");

                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    System.out.println("LỖI: Khoảng ngày không hợp lệ - Bắt đầu: " + startDate + ", Kết thúc: " + endDate);
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/movies/" + id + "/edit";
                }
            } catch (DateTimeParseException e) {
                System.out.println("LỖI: Không thể parse ngày - Bắt đầu: " + startDateStr + ", Kết thúc: " + endDateStr);
                redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ (yyyy-MM-dd)");
                return "redirect:/movies/" + id + "/edit";
            }

            // Tạo đối tượng Movie để cập nhật - ĐÃ SỬA
            Movie movieToUpdate = new Movie();
            movieToUpdate.setMovieID(id); // Đã sửa: sử dụng setMovieID
            movieToUpdate.setMovieName(movieName.trim());
            movieToUpdate.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
            movieToUpdate.setImage(image != null && !image.trim().isEmpty() ? image.trim() : null);
            movieToUpdate.setBanner(banner != null && !banner.trim().isEmpty() ? banner.trim() : null);
            movieToUpdate.setStudio(studio != null && !studio.trim().isEmpty() ? studio.trim() : null);
            movieToUpdate.setDuration(duration);
            movieToUpdate.setTrailer(trailer != null && !trailer.trim().isEmpty() ? trailer.trim() : null);

            // Đặt đánh giá - sử dụng Double thay vì BigDecimal
            movieToUpdate.setMovieRate(movieRate);
            System.out.println("Controller: Đặt đánh giá movieToUpdate = " + movieToUpdate.getMovieRate());

            // Sử dụng LocalDateTime thay vì LocalDate
            movieToUpdate.setStartDate(startDate);
            movieToUpdate.setEndDate(endDate);

            // Đặt trạng thái - ĐÃ SỬA để sử dụng enum đúng
            if ("Active".equals(status)) {
                movieToUpdate.setStatus(Movie_Status.Active);
            } else {
                movieToUpdate.setStatus(Movie_Status.Removed);
            }

            System.out.println("=== DỮ LIỆU CẬP NHẬT PHIM ===");
            System.out.println("Tên cũ: " + existingMovie.getMovieName());
            System.out.println("Tên mới: " + movieToUpdate.getMovieName());
            System.out.println("Đánh giá cũ: " + existingMovie.getMovieRate());
            System.out.println("Đánh giá mới: " + movieToUpdate.getMovieRate());
            System.out.println("Thời lượng mới: " + movieToUpdate.getDuration());
            System.out.println("Ngày bắt đầu mới: " + movieToUpdate.getStartDate());
            System.out.println("Ngày kết thúc mới: " + movieToUpdate.getEndDate());
            System.out.println("Trạng thái mới: " + movieToUpdate.getStatus());
            System.out.println("Thể loại mới: " + genreIds);

            // Debug đánh giá trước khi cập nhật
            System.out.println("=== CONTROLLER: TRƯỚC KHI CẬP NHẬT ===");
            movieService.debugMovieRating(id);

            // Thực hiện cập nhật
            System.out.println("Đang gọi movieService.updateMovieComplete...");
            boolean updateSuccess = movieService.updateMovieComplete(id, movieToUpdate, genreIds);

            // Debug đánh giá sau khi cập nhật
            System.out.println("=== CONTROLLER: SAU KHI CẬP NHẬT ===");
            movieService.debugMovieRating(id);

            if (updateSuccess) {
                System.out.println("THÀNH CÔNG: Phim đã được cập nhật thành công!");

                // Xác minh cập nhật bằng cách lấy phim đã cập nhật
                MovieDTO updatedMovie = movieService.getMovieById(id);
                if (updatedMovie != null) {
                    System.out.println("Đã xác minh - Tên phim đã cập nhật: " + updatedMovie.getMovieName());
                    System.out.println("Đã xác minh - Thời lượng đã cập nhật: " + updatedMovie.getDuration());
                    System.out.println("Đã xác minh - Đánh giá đã cập nhật: " + updatedMovie.getMovieRate());
                }

                redirectAttributes.addFlashAttribute("success",
                        "Cập nhật phim '" + movieName + "' thành công!");
                return "redirect:/movies/" + id;

            } else {
                System.out.println("LỖI: Cập nhật phim thất bại - service trả về false");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật phim");
                return "redirect:/movies/" + id + "/edit";
            }

        } catch (IllegalArgumentException e) {
            System.err.println("LỖI VALIDATION: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/movies/" + id + "/edit";

        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG trong updateMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/movies/" + id + "/edit";
        }
    }

    // ==================== CHỨC NĂNG CHỈNH SỬA NGÀY KẾT THÚC ====================

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
            System.err.println("Lỗi khi tải form sửa ngày kết thúc: " + e.getMessage());
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

            // Parse và chuyển đổi thành LocalDateTime
            LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");
            LocalDateTime startDate = movie.getStartDate();

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
            System.err.println("Lỗi khi cập nhật ngày kết thúc: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi hệ thống xảy ra");
            return "redirect:/movies/" + id + "/edit-enddate";
        }

        return "redirect:/movies/" + id;
    }


    // ==================== CHỨC NĂNG THÊM PHIM - ĐÃ SỬA ====================

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
            System.err.println("Lỗi khi tải form thêm phim: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải form thêm phim");
            return "redirect:/movies";
        }
    }

    /**
     * Xử lý thêm phim mới - PHIÊN BẢN ĐÃ SỬA
     */
    @PostMapping("/add")
    public String addMovie(@RequestParam Map<String, String> allParams,
                           @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                           @RequestParam(value = "actorIds", required = false) List<Integer> actorIds,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {

        System.out.println("=========================");
        System.out.println("=== BẮT ĐẦU THÊM PHIM MỚI ===");
        System.out.println("=========================");
        System.out.println("URL yêu cầu: " + request.getRequestURL());
        System.out.println("Loại nội dung: " + request.getContentType());

        // Ghi log tất cả tham số
        System.out.println("--- TẤT CẢ THAM SỐ FORM ---");
        allParams.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("ID thể loại được chọn: " + genreIds);
        System.out.println("ID diễn viên được chọn: " + actorIds);
        System.out.println("=========================");

        try {
            // Lấy dữ liệu từ form với xử lý NULL
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
                System.out.println("LỖI: Tên phim trống");
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/movies/add";
            }

            // Kiểm tra tên phim trùng lặp
            if (movieService.isMovieNameExists(movieName.trim(), null)) {
                System.out.println("LỖI: Tên phim đã tồn tại: " + movieName);
                redirectAttributes.addFlashAttribute("error", "Tên phim đã tồn tại");
                return "redirect:/movies/add";
            }

            // Parse thời lượng - CHO PHÉP NULL
            Integer duration = null;
            String durationStr = getParameterOrNull(allParams, "duration");
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr.trim());
                    if (duration <= 0 || duration > 500) {
                        System.out.println("LỖI: Thời lượng không hợp lệ: " + duration);
                        redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                        return "redirect:/movies/add";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("LỖI: Không thể parse thời lượng: " + durationStr);
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                    return "redirect:/movies/add";
                }
            }

            // Parse đánh giá phim - CHO PHÉP NULL - ĐÃ SỬA để sử dụng Double
            Double movieRate = null;
            String movieRateStr = getParameterOrNull(allParams, "movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());

                    System.out.println("Controller: Đánh giá đã parse = " + movieRate);

                    if (movieRate < 0.0 || movieRate > 5.0) {
                        System.out.println("LỖI: Đánh giá phim không hợp lệ: " + movieRate);
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/movies/add";
                    }
                } catch (NumberFormatException e) {
                    System.out.println("LỖI: Không thể parse đánh giá phim: " + movieRateStr);
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/movies/add";
                }
            }

            // Parse ngày - CHO PHÉP NULL và chuyển đổi thành LocalDateTime
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    startDate = LocalDateTime.parse(startDateStr.trim() + "T00:00:00");
                } catch (DateTimeParseException e) {
                    System.out.println("LỖI: Không thể parse ngày bắt đầu: " + startDateStr);
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày bắt đầu không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/movies/add";
                }
            }

            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    endDate = LocalDateTime.parse(endDateStr.trim() + "T23:59:59");
                } catch (DateTimeParseException e) {
                    System.out.println("LỖI: Không thể parse ngày kết thúc: " + endDateStr);
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày kết thúc không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/movies/add";
                }
            }

            // Validate khoảng ngày nếu cả hai đều không null
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    System.out.println("LỖI: Khoảng ngày không hợp lệ - Bắt đầu: " + startDate + ", Kết thúc: " + endDate);
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/movies/add";
                }
            }

            // Validate thể loại - CHO PHÉP EMPTY
            if (genreIds != null && !genreIds.isEmpty()) {
                if (genreIds.size() > 5) {
                    System.out.println("LỖI: Quá nhiều thể loại được chọn: " + genreIds.size());
                    redirectAttributes.addFlashAttribute("error", "Chỉ được chọn tối đa 5 thể loại");
                    return "redirect:/movies/add";
                }

                // Validate thể loại có tồn tại
                for (Integer genreId : genreIds) {
                    if (!genreService.existsById(genreId)) {
                        System.out.println("LỖI: Không tìm thấy thể loại: " + genreId);
                        redirectAttributes.addFlashAttribute("error", "Thể loại không tồn tại");
                        return "redirect:/movies/add";
                    }
                }
            }

            // Validate diễn viên - CHO PHÉP EMPTY
            if (actorIds != null && !actorIds.isEmpty()) {
                for (Integer actorId : actorIds) {
                    if (actorService.getActorById(actorId) == null) {
                        System.out.println("LỖI: Không tìm thấy diễn viên: " + actorId);
                        redirectAttributes.addFlashAttribute("error", "Diễn viên không tồn tại");
                        return "redirect:/movies/add";
                    }
                }
            }

            // Tạo đối tượng Movie - ĐÃ SỬA
            Movie newMovie = new Movie();
            newMovie.setMovieName(movieName.trim());
            newMovie.setDescription(description); // có thể null
            newMovie.setImage(image); // có thể null
            newMovie.setBanner(banner); // có thể null
            newMovie.setStudio(studio); // có thể null
            newMovie.setDuration(duration); // có thể null - sẽ được set default
            newMovie.setTrailer(trailer); // có thể null
            newMovie.setMovieRate(movieRate); // Kiểu Double - có thể null

            // Sử dụng LocalDateTime cho entity
            newMovie.setStartDate(startDate);
            newMovie.setEndDate(endDate);

            // Đặt trạng thái - ĐÃ SỬA để sử dụng enum đúng
            if ("Active".equals(status)) {
                newMovie.setStatus(Movie_Status.Active);
            } else if ("Removed".equals(status)) {
                newMovie.setStatus(Movie_Status.Removed);
            } else {
                newMovie.setStatus(null); // sẽ được set default
            }

            System.out.println("=== DỮ LIỆU PHIM MỚI ===");
            System.out.println("Tên: " + newMovie.getMovieName());
            System.out.println("Thời lượng: " + newMovie.getDuration());
            System.out.println("Đánh giá: " + newMovie.getMovieRate());
            System.out.println("Ngày bắt đầu: " + newMovie.getStartDate());
            System.out.println("Ngày kết thúc: " + newMovie.getEndDate());
            System.out.println("Trạng thái: " + newMovie.getStatus());
            System.out.println("Thể loại: " + genreIds);
            System.out.println("Diễn viên: " + actorIds);

            // Thực hiện thêm phim mới
            System.out.println("Đang gọi movieService.addMovie...");
            MovieDTO addedMovie = movieService.addMovie(newMovie, genreIds, actorIds);

            if (addedMovie != null) {
                System.out.println("THÀNH CÔNG: Phim đã được thêm thành công với ID: " + addedMovie.getMovieId());
                redirectAttributes.addFlashAttribute("success",
                        "Thêm phim '" + movieName + "' thành công! Các thông tin chưa nhập sẽ được set giá trị mặc định.");
                return "redirect:/movies/" + addedMovie.getMovieId();
            } else {
                System.out.println("LỖI: Thêm phim thất bại - service trả về null");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm phim");
                return "redirect:/movies/add";
            }

        } catch (IllegalArgumentException e) {
            System.err.println("LỖI VALIDATION: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/movies/add";

        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG trong addMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/movies/add";
        }
    }

    /**
     * Phương thức hỗ trợ để lấy parameter hoặc null nếu empty
     */
    private String getParameterOrNull(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    // ==================== CHỨC NĂNG XÓA - ĐÃ SỬA ====================

    /**
     * Xóa vĩnh viễn phim khỏi database (chỉ cho phim có trạng thái "Removed")
     */
    @PostMapping("/{id}/hard-delete")
    public String hardDeleteMovie(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        System.out.println("=== XÓA VĨNH VIỄN PHIM ===");
        System.out.println("ID phim: " + id);

        try {
            // Kiểm tra phim có tồn tại không
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                System.out.println("LỖI: Không tìm thấy phim với ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/movies";
            }

            // Kiểm tra trạng thái phải là "Removed"
            if (!"Removed".equals(movie.getStatus())) {
                System.out.println("LỖI: Trạng thái phim không phải 'Removed': " + movie.getStatus());
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa vĩnh viễn những phim có trạng thái 'Removed'");
                return "redirect:/movies/" + id;
            }

            String movieName = movie.getMovieName();
            System.out.println("Đang cố gắng xóa vĩnh viễn phim: " + movieName);

            // Thực hiện xóa vĩnh viễn
            boolean deleteSuccess = movieService.hardDeleteMovieComplete(id);

            if (deleteSuccess) {
                System.out.println("THÀNH CÔNG: Phim đã được xóa vĩnh viễn thành công!");
                redirectAttributes.addFlashAttribute("success",
                        "Đã xóa vĩnh viễn phim '" + movieName + "' khỏi hệ thống!");
                return "redirect:/movies";
            } else {
                System.out.println("LỖI: Xóa vĩnh viễn thất bại");
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa phim");
                return "redirect:/movies/" + id;
            }

        } catch (Exception e) {
            System.err.println("LỖI trong hardDeleteMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/movies/" + id;
        }
    }

}