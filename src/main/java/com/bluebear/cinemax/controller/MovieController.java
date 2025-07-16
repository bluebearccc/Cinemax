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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/movies")
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
     * URL: /admin/movies
     * Template: movies.html
     */
    @GetMapping("")
    public String getAllMovies(Model model) {
        List<MovieDTO> movies = movieService.getAllMovies();
        List<Genre> genres = genreService.getAllGenres();

        model.addAttribute("movies", movies);
        model.addAttribute("genres", genres);
        model.addAttribute("pageTitle", "Tất cả phim");

        return "admin/movies"; // -> templates/admin/movies.html
    }

    /**
     * Phim Active - THÊM METHOD MỚI
     * URL: /admin/movies/active
     */
    @GetMapping("/active")
    public String getActiveMovies(Model model) {
        try {
            List<MovieDTO> movies = movieService.getAllActiveMovies();
            List<Genre> genres = genreService.getAllGenres();

            model.addAttribute("movies", movies);
            model.addAttribute("genres", genres);
            model.addAttribute("pageTitle", "Phim Active");

            return "admin/movies";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải phim Active: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải phim Active");
            return "admin/movies";
        }
    }

    /**
     * Phim đang chiếu
     * URL: /admin/movies/now-showing
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
     * URL: /admin/movies/upcoming
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
     * URL: /admin/movies/{id}
     * Template: detail.html
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

            return "admin/detail"; // -> templates/admin/detail.html
        } catch (Exception e) {
            System.err.println("Lỗi khi tải chi tiết phim: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin phim");
            return "error/500";
        }
    }

    // ==================== TÌM KIẾM VÀ LỌC ====================

    /**
     * Tìm kiếm phim - SỬA LẠI LOGIC VÀ THÊM DEBUG
     * URL: /admin/movies/search
     */
    @GetMapping("/search")
    public String searchMovies(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer genreId,
                               @RequestParam(required = false) String status,
                               Model model,
                               HttpServletRequest request) {

        System.out.println("=== SEARCH REQUEST DEBUG ===");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Query String: " + request.getQueryString());
        System.out.println("Parameters:");
        System.out.println("  keyword: '" + keyword + "'");
        System.out.println("  genreId: " + genreId);
        System.out.println("  status: '" + status + "'");
        System.out.println("============================");

        try {
            List<MovieDTO> movies;
            String pageTitle = "Kết quả tìm kiếm";
            StringBuilder titleBuilder = new StringBuilder();

            // Bước 1: Lọc theo Trạng thái TRƯỚC
            if (status != null && !status.trim().isEmpty()) {
                switch (status.trim()) {
                    case "Active":
                        movies = movieService.getAllActiveMovies();
                        titleBuilder.append("Phim Active");
                        break;
                    case "Removed":
                        movies = movieService.getAllMovies().stream()
                                .filter(movie -> "Removed".equals(movie.getStatus()))
                                .collect(Collectors.toList());
                        titleBuilder.append("Phim Removed");
                        break;
                    case "NowShowing":
                        movies = movieService.getNowShowingMovies();
                        titleBuilder.append("Phim đang chiếu");
                        break;
                    case "Upcoming":
                        movies = movieService.getUpcomingMovies();
                        titleBuilder.append("Phim sắp chiếu");
                        break;
                    default:
                        movies = movieService.getAllMovies();
                        titleBuilder.append("Tất cả phim");
                        break;
                }
            } else {
                movies = movieService.getAllMovies();
                titleBuilder.append("Tất cả phim");
            }

            System.out.println("Movies found after status filter: " + movies.size());

            // Bước 2: Lọc theo từ khóa
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchKeyword = keyword.toLowerCase().trim();
                movies = movies.stream()
                        .filter(movie -> movie.getMovieName().toLowerCase().contains(searchKeyword))
                        .collect(Collectors.toList());

                if (titleBuilder.length() > 0) titleBuilder.append(" | ");
                titleBuilder.append("Tìm kiếm: \"").append(keyword).append("\"");

                System.out.println("Movies found after keyword filter: " + movies.size());
            }

            // Bước 3: Lọc theo thể loại
            if (genreId != null && genreId > 0) {
                movies = movies.stream()
                        .filter(movie -> movie.getGenres() != null &&
                                movie.getGenres().stream().anyMatch(genreName -> {
                                    Genre g = genreService.getGenreById(genreId);
                                    return g != null && genreName.equals(g.getGenreName());
                                }))
                        .collect(Collectors.toList());

                Genre genre = genreService.getGenreById(genreId);
                if (genre != null) {
                    if (titleBuilder.length() > 0) titleBuilder.append(" | ");
                    titleBuilder.append("Thể loại: ").append(genre.getGenreName());
                }

                System.out.println("Movies found after genre filter: " + movies.size());
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

            System.out.println("Final result count: " + movies.size());
            System.out.println("===================");

            return "admin/movies";

        } catch (Exception e) {
            System.err.println("Lỗi trong tìm kiếm: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm: " + e.getMessage());
            model.addAttribute("movies", new ArrayList<>());
            model.addAttribute("genres", genreService.getAllGenres());
            model.addAttribute("pageTitle", "Lỗi tìm kiếm");
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
     * Hiển thị form chỉnh sửa phim
     * URL: /admin/movies/{id}/edit
     * Template: edit.html
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
                return "redirect:/admin/movies"; // ✅ FIXED: Đã sửa từ /movies thành /admin/movies
            }

            // Lấy tất cả thể loại
            List<Genre> allGenres = genreService.getAllGenres();
            System.out.println("Tổng số thể loại có sẵn: " + allGenres.size());

            // Lấy các ID thể loại hiện tại của phim
            List<Integer> movieGenreIds = genreService.getGenresByMovie(id).stream()
                    .map(Genre::getGenreID)
                    .collect(Collectors.toList());
            System.out.println("Thể loại hiện tại của phim: " + movieGenreIds);

            model.addAttribute("movie", movie);
            model.addAttribute("allGenres", allGenres);
            model.addAttribute("movieGenreIds", movieGenreIds);
            model.addAttribute("pageTitle", "Chỉnh sửa phim - " + movie.getMovieName());

            System.out.println("Đã tải thành công form chỉnh sửa cho phim: " + movie.getMovieName());
            return "admin/edit"; // -> templates/admin/edit.html

        } catch (Exception e) {
            System.err.println("Lỗi khi tải form chỉnh sửa: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải form chỉnh sửa");
            return "redirect:/admin/movies"; // ✅ FIXED
        }
    }

    /**
     * Xử lý cập nhật phim
     * URL: POST /admin/movies/{id}/edit
     */
    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable Integer id,
                              @RequestParam Map<String, String> allParams,
                              @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {

        System.out.println("=== BẮT ĐẦU CẬP NHẬT PHIM ===");
        System.out.println("ID phim: " + id);

        try {
            // Kiểm tra phim có tồn tại không
            MovieDTO existingMovie = movieService.getMovieById(id);
            if (existingMovie == null) {
                System.out.println("LỖI: Không tìm thấy phim với ID: " + id);
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim để cập nhật");
                return "redirect:/admin/movies"; // ✅ FIXED
            }

            System.out.println("Đã tìm thấy phim hiện tại: " + existingMovie.getMovieName());

            // ... (giữ nguyên phần validation code) ...

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
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
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
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                    return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
            }

            // Validate và parse đánh giá phim
            Double movieRate = null;
            String movieRateStr = allParams.get("movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());
                    if (movieRate < 0.0 || movieRate > 5.0) {
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
                    }
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
                }
            }

            // Validate và parse ngày
            LocalDateTime startDate, endDate;
            try {
                if (startDateStr == null || startDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("Ngày bắt đầu trống", "", 0);
                }
                if (endDateStr == null || endDateStr.trim().isEmpty()) {
                    throw new DateTimeParseException("Ngày kết thúc trống", "", 0);
                }

                startDate = LocalDateTime.parse(startDateStr.trim() + "T00:00:00");
                endDate = LocalDateTime.parse(endDateStr.trim() + "T23:59:59");

                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
                }
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ (yyyy-MM-dd)");
                return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
            }

            // Tạo đối tượng Movie để cập nhật
            Movie movieToUpdate = new Movie();
            movieToUpdate.setMovieID(id);
            movieToUpdate.setMovieName(movieName.trim());
            movieToUpdate.setDescription(description != null && !description.trim().isEmpty() ? description.trim() : null);
            movieToUpdate.setImage(image != null && !image.trim().isEmpty() ? image.trim() : null);
            movieToUpdate.setBanner(banner != null && !banner.trim().isEmpty() ? banner.trim() : null);
            movieToUpdate.setStudio(studio != null && !studio.trim().isEmpty() ? studio.trim() : null);
            movieToUpdate.setDuration(duration);
            movieToUpdate.setTrailer(trailer != null && !trailer.trim().isEmpty() ? trailer.trim() : null);
            movieToUpdate.setMovieRate(movieRate);
            movieToUpdate.setStartDate(startDate);
            movieToUpdate.setEndDate(endDate);

            // Đặt trạng thái
            if ("Active".equals(status)) {
                movieToUpdate.setStatus(Movie_Status.Active);
            } else {
                movieToUpdate.setStatus(Movie_Status.Removed);
            }

            // Thực hiện cập nhật
            boolean updateSuccess = movieService.updateMovieComplete(id, movieToUpdate, genreIds);

            if (updateSuccess) {
                redirectAttributes.addFlashAttribute("success",
                        "Cập nhật phim '" + movieName + "' thành công!");
                return "redirect:/admin/movies/" + id; // ✅ FIXED
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật phim");
                return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG trong updateMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/admin/movies/" + id + "/edit"; // ✅ FIXED
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
                return "redirect:/admin/movies"; // ✅ FIXED
            }

            model.addAttribute("movie", movie);
            model.addAttribute("pageTitle", "Sửa ngày hết hạn chiếu - " + movie.getMovieName());

            return "admin/edit-enddate";
        } catch (Exception e) {
            System.err.println("Lỗi khi tải form sửa ngày kết thúc: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải form");
            return "redirect:/admin/movies"; // ✅ FIXED
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
                return "redirect:/admin/movies"; // ✅ FIXED
            }

            LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T23:59:59");
            LocalDateTime startDate = movie.getStartDate();

            if (startDate != null && endDate.isBefore(startDate)) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc không thể trước ngày bắt đầu");
                return "redirect:/admin/movies/" + id + "/edit-enddate"; // ✅ FIXED
            }

            boolean success = movieService.updateEndDate(id, endDate);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật ngày hết hạn chiếu thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật");
            }

        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Định dạng ngày không hợp lệ");
            return "redirect:/admin/movies/" + id + "/edit-enddate"; // ✅ FIXED
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật ngày kết thúc: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi hệ thống xảy ra");
            return "redirect:/admin/movies/" + id + "/edit-enddate"; // ✅ FIXED
        }

        return "redirect:/admin/movies/" + id; // ✅ FIXED
    }

    // ==================== CHỨC NĂNG THÊM PHIM ====================

    /**
     * Hiển thị form thêm phim mới
     * URL: /admin/movies/add
     * Template: add-movie.html
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        try {
            List<Genre> allGenres = genreService.getAllGenres();
            List<ActorDTO> allActors = actorService.getAllActors();

            model.addAttribute("allGenres", allGenres);
            model.addAttribute("allActors", allActors);
            model.addAttribute("pageTitle", "Thêm phim mới");

            return "admin/add-movie"; // -> templates/admin/add-movie.html
        } catch (Exception e) {
            System.err.println("Lỗi khi tải form thêm phim: " + e.getMessage());
            model.addAttribute("error", "Có lỗi xảy ra khi tải form thêm phim");
            return "redirect:/admin/movies"; // ✅ FIXED
        }
    }

    /**
     * Xử lý thêm phim mới
     * URL: POST /admin/movies/add
     */
    @PostMapping("/add")
    public String addMovie(@RequestParam Map<String, String> allParams,
                           @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                           @RequestParam(value = "actorIds", required = false) List<Integer> actorIds,
                           RedirectAttributes redirectAttributes,
                           HttpServletRequest request) {

        System.out.println("=== BẮT ĐẦU THÊM PHIM MỚI ===");

        try {
            // Lấy dữ liệu từ form
            String movieName = getParameterOrNull(allParams, "movieName");

            // Validate tên phim - CHỈ CÓ TÊN PHIM LÀ BẮT BUỘC
            if (movieName == null || movieName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên phim không được để trống");
                return "redirect:/admin/movies/add"; // ✅ FIXED
            }

            // Kiểm tra tên phim trùng lặp
            if (movieService.isMovieNameExists(movieName.trim(), null)) {
                redirectAttributes.addFlashAttribute("error", "Tên phim đã tồn tại");
                return "redirect:/admin/movies/add"; // ✅ FIXED
            }

            // ... (giữ nguyên phần validation code khác) ...

            String description = getParameterOrNull(allParams, "description");
            String image = getParameterOrNull(allParams, "image");
            String banner = getParameterOrNull(allParams, "banner");
            String studio = getParameterOrNull(allParams, "studio");
            String trailer = getParameterOrNull(allParams, "trailer");
            String startDateStr = getParameterOrNull(allParams, "startDate");
            String endDateStr = getParameterOrNull(allParams, "endDate");
            String status = getParameterOrNull(allParams, "status");

            // Parse thời lượng
            Integer duration = null;
            String durationStr = getParameterOrNull(allParams, "duration");
            if (durationStr != null && !durationStr.trim().isEmpty()) {
                try {
                    duration = Integer.parseInt(durationStr.trim());
                    if (duration <= 0 || duration > 500) {
                        redirectAttributes.addFlashAttribute("error", "Thời lượng phim phải từ 1-500 phút");
                        return "redirect:/admin/movies/add"; // ✅ FIXED
                    }
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("error", "Thời lượng phim không hợp lệ");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }
            }

            // Parse đánh giá phim
            Double movieRate = null;
            String movieRateStr = getParameterOrNull(allParams, "movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    movieRate = Double.parseDouble(movieRateStr.trim());
                    if (movieRate < 0.0 || movieRate > 5.0) {
                        redirectAttributes.addFlashAttribute("error", "Đánh giá phim phải từ 0-5");
                        return "redirect:/admin/movies/add"; // ✅ FIXED
                    }
                } catch (NumberFormatException e) {
                    redirectAttributes.addFlashAttribute("error", "Đánh giá phim không hợp lệ");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }
            }

            // Parse ngày
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;

            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                try {
                    startDate = LocalDateTime.parse(startDateStr.trim() + "T00:00:00");
                } catch (DateTimeParseException e) {
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày bắt đầu không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }
            }

            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    endDate = LocalDateTime.parse(endDateStr.trim() + "T23:59:59");
                } catch (DateTimeParseException e) {
                    redirectAttributes.addFlashAttribute("error", "Định dạng ngày kết thúc không hợp lệ (yyyy-MM-dd)");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }
            }

            // Validate khoảng ngày
            if (startDate != null && endDate != null) {
                if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
                    redirectAttributes.addFlashAttribute("error", "Ngày bắt đầu phải trước ngày kết thúc");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }
            }

            // Validate thể loại
            if (genreIds != null && !genreIds.isEmpty()) {
                if (genreIds.size() > 5) {
                    redirectAttributes.addFlashAttribute("error", "Chỉ được chọn tối đa 5 thể loại");
                    return "redirect:/admin/movies/add"; // ✅ FIXED
                }

                for (Integer genreId : genreIds) {
                    if (!genreService.existsById(genreId)) {
                        redirectAttributes.addFlashAttribute("error", "Thể loại không tồn tại");
                        return "redirect:/admin/movies/add"; // ✅ FIXED
                    }
                }
            }

            // Validate diễn viên
            if (actorIds != null && !actorIds.isEmpty()) {
                for (Integer actorId : actorIds) {
                    if (actorService.getActorById(actorId) == null) {
                        redirectAttributes.addFlashAttribute("error", "Diễn viên không tồn tại");
                        return "redirect:/admin/movies/add"; // ✅ FIXED
                    }
                }
            }

            // Tạo đối tượng Movie
            Movie newMovie = new Movie();
            newMovie.setMovieName(movieName.trim());
            newMovie.setDescription(description);
            newMovie.setImage(image);
            newMovie.setBanner(banner);
            newMovie.setStudio(studio);
            newMovie.setDuration(duration);
            newMovie.setTrailer(trailer);
            newMovie.setMovieRate(movieRate);
            newMovie.setStartDate(startDate);
            newMovie.setEndDate(endDate);

            // Đặt trạng thái
            if ("Active".equals(status)) {
                newMovie.setStatus(Movie_Status.Active);
            } else if ("Removed".equals(status)) {
                newMovie.setStatus(Movie_Status.Removed);
            } else {
                newMovie.setStatus(null); // sẽ được set default
            }

            // Thực hiện thêm phim mới
            MovieDTO addedMovie = movieService.addMovie(newMovie, genreIds, actorIds);

            if (addedMovie != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Thêm phim '" + movieName + "' thành công! Các thông tin chưa nhập sẽ được set giá trị mặc định.");
                return "redirect:/admin/movies/" + addedMovie.getMovieId(); // ✅ FIXED
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm phim");
                return "redirect:/admin/movies/add"; // ✅ FIXED
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/movies/add"; // ✅ FIXED
        } catch (Exception e) {
            System.err.println("LỖI NGHIÊM TRỌNG trong addMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi hệ thống xảy ra: " + e.getMessage());
            return "redirect:/admin/movies/add"; // ✅ FIXED
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

    // ==================== CHỨC NĂNG XÓA ====================

    /**
     * Xóa vĩnh viễn phim khỏi database (chỉ cho phim có trạng thái "Removed")
     */
    @PostMapping("/{id}/hard-delete")
    public String hardDeleteMovie(@PathVariable Integer id, RedirectAttributes redirectAttributes) {

        System.out.println("=== XÓA VĨNH VIỄN PHIM ===");
        System.out.println("ID phim: " + id);

        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/admin/movies"; // ✅ FIXED
            }

            // Kiểm tra trạng thái phải là "Removed"
            if (!"Removed".equals(movie.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa vĩnh viễn những phim có trạng thái 'Removed'");
                return "redirect:/admin/movies/" + id; // ✅ FIXED
            }

            String movieName = movie.getMovieName();

            // Thực hiện xóa vĩnh viễn
            boolean deleteSuccess = movieService.hardDeleteMovieComplete(id);

            if (deleteSuccess) {
                redirectAttributes.addFlashAttribute("success",
                        "Đã xóa vĩnh viễn phim '" + movieName + "' khỏi hệ thống!");
                return "redirect:/admin/movies"; // ✅ FIXED
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa phim");
                return "redirect:/admin/movies/" + id; // ✅ FIXED
            }

        } catch (Exception e) {
            System.err.println("LỖI trong hardDeleteMovie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/movies/" + id; // ✅ FIXED
        }
    }



}