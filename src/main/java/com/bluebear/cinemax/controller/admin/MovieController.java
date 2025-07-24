package com.bluebear.cinemax.controller.admin;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.repository.repos.*;
import com.bluebear.cinemax.service.admins.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller("movieAdminController")
@RequestMapping("/admin/movies")
public class MovieController {

    @Autowired private MovieService movieService;
    @Autowired private ActorService actorService;
    @Autowired private GenreService genreService;
    @Autowired private GenreRepository genreRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ActorRepository actorRepository;

    private static final String UPLOAD_DIR = "uploads/";

    // ==================== UPLOAD ẢNH ĐƠN GIẢN ====================

    /**
     * Upload ảnh movie - tạo tên file duy nhất với timestamp
     */
    private String uploadMovieImage(MultipartFile file, String type, Integer movieId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Tạo thư mục nếu chưa tồn tại
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            System.out.println("Created upload directory: " + created);
        }

        // Lấy extension
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        } else {
            fileExtension = ".jpg"; // default
        }

        // Tạo tên file duy nhất: movie_{movieId}_{type}_{timestamp}.{extension}
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = "movie_" + movieId + "_" + type + "_" + timestamp + fileExtension;
        Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);

        // Lưu file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Uploaded file: " + filePath.toAbsolutePath());

        // Trả về đường dẫn web
        return "/uploads/" + uniqueFilename;
    }

    // ==================== VALIDATION METHODS ====================

    private String validateRequiredData(Map<String, String> params, boolean isEdit) {
        String movieName = params.get("movieName");
        if (movieName == null || movieName.trim().isEmpty()) return "Tên phim không được để trống";

        String description = params.get("description");
        if (description == null || description.trim().isEmpty()) return "Mô tả phim không được để trống";

        String studio = params.get("studio");
        if (studio == null || studio.trim().isEmpty()) return "Hãng sản xuất không được để trống";

        String durationStr = params.get("duration");
        if (durationStr == null || durationStr.trim().isEmpty()) return "Thời lượng phim không được để trống";
        try {
            int duration = Integer.parseInt(durationStr.trim());
            if (duration <= 0 || duration > 500) return "Thời lượng phim phải từ 1-500 phút";
        } catch (NumberFormatException e) {
            return "Thời lượng phim không hợp lệ";
        }

        String status = params.get("status");
        if (status == null || status.trim().isEmpty()) return "Trạng thái phim không được để trống";

        // Chỉ validate movieRate khi edit, không validate khi add
        if (isEdit) {
            String movieRateStr = params.get("movieRate");
            if (movieRateStr != null && !movieRateStr.trim().isEmpty()) {
                try {
                    double movieRate = Double.parseDouble(movieRateStr.trim());
                    if (movieRate < 0.0 || movieRate > 5.0) return "Đánh giá phim phải từ 0.0 đến 5.0";
                } catch (NumberFormatException e) {
                    return "Đánh giá phim không hợp lệ";
                }
            }
        }

        return null;
    }

    private String validateRequiredImages(MultipartFile posterFile, MultipartFile bannerFile, boolean isAdd) {
        // Chỉ validate khi thêm mới (isAdd = true), khi edit thì không bắt buộc
        if (!isAdd) {
            return null;
        }

        // Kiểm tra poster file
        if (posterFile == null || posterFile.isEmpty()) {
            return "Phải tải lên ảnh poster cho phim";
        }

        // Kiểm tra banner file
        if (bannerFile == null || bannerFile.isEmpty()) {
            return "Phải tải lên ảnh banner cho phim";
        }

        // Kiểm tra loại file poster
        if (!posterFile.getContentType().startsWith("image/")) {
            return "Poster phải là file hình ảnh";
        }

        // Kiểm tra loại file banner
        if (!bannerFile.getContentType().startsWith("image/")) {
            return "Banner phải là file hình ảnh";
        }

        // Kiểm tra kích thước file poster (5MB = 5 * 1024 * 1024 bytes)
        if (posterFile.getSize() > 5 * 1024 * 1024) {
            return "Poster không được vượt quá 5MB";
        }

        // Kiểm tra kích thước file banner
        if (bannerFile.getSize() > 5 * 1024 * 1024) {
            return "Banner không được vượt quá 5MB";
        }

        return null;
    }

    private String validateDateParams(Map<String, String> params, boolean isEdit) {
        String startDateStr = params.get("startDate");
        String endDateStr = params.get("endDate");

        if (!isEdit) {
            if (startDateStr == null || startDateStr.trim().isEmpty()) {
                return "Ngày bắt đầu chiếu không được để trống";
            }
            if (endDateStr == null || endDateStr.trim().isEmpty()) {
                return "Ngày kết thúc chiếu không được để trống";
            }
        }

        if (endDateStr != null && !endDateStr.trim().isEmpty() && startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                LocalDateTime endDate = LocalDateTime.parse(endDateStr);
                LocalDateTime startDate = LocalDateTime.parse(startDateStr);
                if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
                    return "Ngày kết thúc phải sau ngày bắt đầu";
                }
            } catch (DateTimeParseException e) {
                return "Định dạng ngày không hợp lệ";
            }
        }

        return null;
    }

    private String validateSelections(List<Integer> genreIds, List<Integer> actorIds) {
        if (genreIds == null || genreIds.isEmpty()) return "Phải chọn ít nhất một thể loại";
        if (genreIds.size() > 5) return "Chỉ được chọn tối đa 5 thể loại";

        if (actorIds == null || actorIds.isEmpty()) return "Phải chọn ít nhất một diễn viên";
        if (actorIds.size() > 10) return "Chỉ được chọn tối đa 10 diễn viên";

        return null;
    }



    // ==================== VIEW METHODS ====================

    @GetMapping("")
    public String getAllMovies(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "genreId", required = false) Integer genreId,
                               @RequestParam(value = "status", required = false) String status,
                               Model model) {
        try {
            List<MovieDTO> allMovies = movieService.getAllMovies();
            List<MovieDTO> filteredMovies = allMovies;

            // Apply filters if any search parameters are provided
            if ((keyword != null && !keyword.trim().isEmpty()) ||
                    genreId != null ||
                    (status != null && !status.trim().isEmpty())) {

                filteredMovies = allMovies.stream()
                        .filter(movie -> {
                            boolean matches = true;

                            if (keyword != null && !keyword.trim().isEmpty()) {
                                matches = matches && movie.getMovieName().toLowerCase()
                                        .contains(keyword.toLowerCase().trim());
                            }

                            if (status != null && !status.trim().isEmpty()) {
                                matches = matches && status.equals(movie.getStatus());
                            }

                            return matches;
                        })
                        .collect(Collectors.toList());
            }

            List<GenreDTO> genres = genreService.getAllGenres();

            // Tính toán thống kê dựa trên ngày
            LocalDateTime now = LocalDateTime.now();
            int totalMovies = allMovies != null ? allMovies.size() : 0;
            int nowShowingCount = 0;
            int upcomingCount = 0;

            if (allMovies != null) {
                for (MovieDTO movie : allMovies) {
                    // Chỉ tính những phim có status Active hoặc Coming_Soon
                    String movieStatus = movie.getStatus().name();
                    if (!"Active".equals(movieStatus) && !"Coming_Soon".equals(movieStatus)) {
                        continue;
                    }

                    LocalDateTime startDate = movie.getStartDate();
                    if (startDate != null) {
                        if (startDate.isAfter(now)) {
                            upcomingCount++; // Phim chưa chiếu
                        } else {
                            LocalDateTime endDate = movie.getEndDate();
                            if (endDate == null || endDate.isAfter(now)) {
                                nowShowingCount++; // Phim đang chiếu
                            }
                        }
                    }
                }
            }

            model.addAttribute("movies", filteredMovies != null ? filteredMovies : new ArrayList<>());
            model.addAttribute("genres", genres != null ? genres : new ArrayList<>());

            model.addAttribute("totalMovies", totalMovies);
            model.addAttribute("nowShowingCount", nowShowingCount);
            model.addAttribute("upcomingCount", upcomingCount);

            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("selectedStatus", status);

            model.addAttribute("pageTitle", "Movie Management");
            return "admin/movies";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("movies", new ArrayList<>());
            model.addAttribute("genres", new ArrayList<>());
            model.addAttribute("totalMovies", 0);
            model.addAttribute("nowShowingCount", 0);
            model.addAttribute("upcomingCount", 0);
            return "admin/movies";
        }
    }

    @GetMapping("/{id}")
    public String getMovieDetail(@PathVariable Integer id, Model model) {
        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                model.addAttribute("error", "Không tìm thấy phim");
                return "error/404";
            }

            model.addAttribute("movie", movie);
            model.addAttribute("actors", actorService.getActorsByMovie(id));
            model.addAttribute("genres", genreService.getGenresByMovie(id));
            model.addAttribute("relatedMovies", movieService.getRelatedMovies(id.longValue()));
            model.addAttribute("pageTitle", movie.getMovieName());
            return "admin/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "error/500";
        }
    }

    // ==================== ADD MOVIE ====================

    @GetMapping("/add")
    public String showAddForm(Model model) {
        try {
            model.addAttribute("genres", genreRepository.findAll());
            model.addAttribute("actors", actorService.getAllActors());
            model.addAttribute("pageTitle", "Thêm phim mới");
            return "admin/add-movie";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/movies";
        }
    }

    @PostMapping("/add")
    public String addMovie(@RequestParam Map<String, String> allParams,
                           @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                           @RequestParam(value = "actorIds", required = false) List<Integer> actorIds,
                           @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
                           @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
                           RedirectAttributes redirectAttributes) {
        try {
            // Validate required data
            String validationError = validateRequiredData(allParams, false);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("error", validationError);
                return "redirect:/admin/movies/add";
            }

            // Validate required images - BẮT BUỘC KHI THÊM MỚI
            String imageValidationError = validateRequiredImages(posterFile, bannerFile, true);
            if (imageValidationError != null) {
                redirectAttributes.addFlashAttribute("error", imageValidationError);
                return "redirect:/admin/movies/add";
            }

            String dateError = validateDateParams(allParams, false);
            if (dateError != null) {
                redirectAttributes.addFlashAttribute("error", dateError);
                return "redirect:/admin/movies/add";
            }

            String selectionError = validateSelections(genreIds, actorIds);
            if (selectionError != null) {
                redirectAttributes.addFlashAttribute("error", selectionError);
                return "redirect:/admin/movies/add";
            }

            // Create movie object với default images và rating
            Movie newMovie = new Movie();
            newMovie.setMovieName(allParams.get("movieName").trim());
            newMovie.setDescription(allParams.get("description").trim());
            newMovie.setStudio(allParams.get("studio").trim());
            newMovie.setDuration(Integer.parseInt(allParams.get("duration").trim()));
            newMovie.setTrailer(allParams.get("trailer") != null && !allParams.get("trailer").trim().isEmpty() ?
                    allParams.get("trailer").trim() : null);

            // Set default rating to 0.0 for new movies
            newMovie.setMovieRate(0.0);

            newMovie.setStartDate(LocalDateTime.parse(allParams.get("startDate")));
            newMovie.setEndDate(LocalDateTime.parse(allParams.get("endDate")));
            newMovie.setStatus(Movie_Status.valueOf(allParams.get("status")));

            // Set default images trước (sẽ được thay thế bằng ảnh upload)
            newMovie.setImage("/uploads/default-movie.jpg");
            newMovie.setBanner("/uploads/default-banner.jpg");

            // Save movie để có ID
            MovieDTO savedMovieDTO = movieService.addMovie(newMovie, genreIds, actorIds);
            Integer savedMovieId = savedMovieDTO.getMovieID();

            // Upload ảnh - BẮT BUỘC
            String finalImagePath = null;
            String finalBannerPath = null;

            try {
                // Upload poster - BẮT BUỘC
                if (posterFile != null && !posterFile.isEmpty()) {
                    finalImagePath = uploadMovieImage(posterFile, "poster", savedMovieId);
                    if (finalImagePath == null) {
                        throw new IOException("Không thể upload ảnh poster");
                    }
                    System.out.println("Uploaded poster: " + finalImagePath);
                } else {
                    throw new IOException("Poster file bị thiếu");
                }

                // Upload banner - BẮT BUỘC
                if (bannerFile != null && !bannerFile.isEmpty()) {
                    finalBannerPath = uploadMovieImage(bannerFile, "banner", savedMovieId);
                    if (finalBannerPath == null) {
                        throw new IOException("Không thể upload ảnh banner");
                    }
                    System.out.println("Uploaded banner: " + finalBannerPath);
                } else {
                    throw new IOException("Banner file bị thiếu");
                }

                // Update movie với đường dẫn ảnh thực
                Movie movieToUpdate = movieRepository.findById(savedMovieId).orElse(null);
                if (movieToUpdate != null) {
                    movieToUpdate.setImage(finalImagePath);
                    movieToUpdate.setBanner(finalBannerPath);
                    movieRepository.save(movieToUpdate);
                    System.out.println("Updated movie images - Poster: " + finalImagePath + ", Banner: " + finalBannerPath);
                }

            } catch (IOException e) {
                System.err.println("Error uploading images: " + e.getMessage());

                // Nếu upload ảnh thất bại, xóa movie đã tạo vì ảnh là bắt buộc
                try {
                    movieRepository.deleteById(savedMovieId);
                    System.out.println("Deleted movie due to image upload failure: " + savedMovieId);
                } catch (Exception deleteEx) {
                    System.err.println("Error deleting movie after image upload failure: " + deleteEx.getMessage());
                }

                redirectAttributes.addFlashAttribute("error", "Lỗi upload ảnh: " + e.getMessage() + ". Phim không được tạo do ảnh là bắt buộc.");
                return "redirect:/admin/movies/add";
            }

            redirectAttributes.addFlashAttribute("success", "Thêm phim thành công với poster và banner!");
            return "redirect:/admin/movies/" + savedMovieId;

        } catch (Exception e) {
            System.err.println("Error adding movie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies/add";
        }
    }

    // ==================== EDIT MOVIE ====================

    @GetMapping("/{id}/edit")
    @Transactional
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieDTO movie = movieService.getMovieById(id);
            if (movie == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/admin/movies";
            }

            List<Genre> allGenres = genreRepository.findAll();
            List<ActorDTO> allActors = actorService.getAllActors();

            Movie movieEntity = movieRepository.findById(id).orElse(null);
            List<Integer> movieGenreIds = new ArrayList<>();
            List<Integer> movieActorIds = new ArrayList<>();

            if (movieEntity != null) {
                if (movieEntity.getGenres() != null) {
                    movieGenreIds = movieEntity.getGenres().stream()
                            .map(Genre::getGenreID)
                            .collect(Collectors.toList());
                }

                if (movieEntity.getActors() != null) {
                    movieActorIds = new ArrayList<>();
                    for (Object actor : movieEntity.getActors()) {
                        try {
                            Method getIdMethod = actor.getClass().getMethod("getId");
                            Integer actorId = (Integer) getIdMethod.invoke(actor);
                            if (actorId != null) {
                                movieActorIds.add(actorId);
                            }
                        } catch (Exception e) {
                            try {
                                Method getActorIdMethod = actor.getClass().getMethod("getActorId");
                                Integer actorId = (Integer) getActorIdMethod.invoke(actor);
                                if (actorId != null) {
                                    movieActorIds.add(actorId);
                                }
                            } catch (Exception e2) {
                                // Ignore
                            }
                        }
                    }
                }
            }

            model.addAttribute("movie", movie);
            model.addAttribute("genres", allGenres);
            model.addAttribute("actors", allActors);
            model.addAttribute("movieGenreIds", movieGenreIds);
            model.addAttribute("movieActorIds", movieActorIds);
            model.addAttribute("pageTitle", "Chỉnh sửa phim: " + movie.getMovieName());

            return "admin/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies";
        }
    }

    private Map<String, Integer> calculateMovieStats() {
        try {
            List<MovieDTO> allMovies = movieService.getAllMovies();
            Map<String, Integer> stats = new HashMap<>();

            int totalMovies = allMovies != null ? allMovies.size() : 0;
            int nowShowingCount = 0;
            int upcomingCount = 0;

            if (allMovies != null) {
                for (MovieDTO movie : allMovies) {
                    String status = movie.getStatus().name();
                    if ("Active".equals(status)) {
                        nowShowingCount++;
                    } else if ("Coming_Soon".equals(status)) {
                        upcomingCount++;
                    }
                }
            }

            stats.put("totalMovies", totalMovies);
            stats.put("nowShowingCount", nowShowingCount);
            stats.put("upcomingCount", upcomingCount);

            return stats;
        } catch (Exception e) {
            Map<String, Integer> emptyStats = new HashMap<>();
            emptyStats.put("totalMovies", 0);
            emptyStats.put("nowShowingCount", 0);
            emptyStats.put("upcomingCount", 0);
            return emptyStats;
        }
    }

    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable Integer id,
                              @RequestParam Map<String, String> allParams,
                              @RequestParam(value = "genreIds", required = false) List<Integer> genreIds,
                              @RequestParam(value = "actorIds", required = false) List<Integer> actorIds,
                              @RequestParam(value = "posterFile", required = false) MultipartFile posterFile,
                              @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
                              RedirectAttributes redirectAttributes) {
        try {
            MovieDTO existingMovie = movieService.getMovieById(id);
            if (existingMovie == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy phim");
                return "redirect:/admin/movies";
            }

            // Validate
            String validationError = validateRequiredData(allParams, true);
            if (validationError != null) {
                redirectAttributes.addFlashAttribute("error", validationError);
                return "redirect:/admin/movies/" + id + "/edit";
            }

            String selectionError = validateSelections(genreIds, actorIds);
            if (selectionError != null) {
                redirectAttributes.addFlashAttribute("error", selectionError);
                return "redirect:/admin/movies/" + id + "/edit";
            }

            // KIỂM TRA STATUS CHANGE - ĐẶC BIỆT CHO REMOVED
            String newStatus = allParams.get("status");
            if ("Removed".equals(newStatus) && !"Removed".equals(existingMovie.getStatus())) {
                // Kiểm tra xem phim có trong schedule không
                if (movieService.hasSchedule(id)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Không thể chuyển phim sang trạng thái 'Đã xóa' vì phim này đang có lịch chiếu trong hệ thống. " +
                                    "Vui lòng xóa tất cả lịch chiếu trước khi thay đổi trạng thái.");
                    return "redirect:/admin/movies/" + id + "/edit";
                }

                // Kiểm tra xem có schedule đang hoạt động không
                if (movieService.hasActiveSchedule(id)) {
                    redirectAttributes.addFlashAttribute("error",
                            "Không thể chuyển phim sang trạng thái 'Đã xóa' vì phim này có lịch chiếu đang hoạt động. " +
                                    "Vui lòng dừng hoặc xóa tất cả lịch chiếu đang hoạt động trước.");
                    return "redirect:/admin/movies/" + id + "/edit";
                }
            }

            // Giữ ảnh cũ làm mặc định
            String finalImagePath = existingMovie.getImage();
            String finalBannerPath = existingMovie.getBanner();

            // Upload ảnh mới nếu có
            try {
                if (posterFile != null && !posterFile.isEmpty()) {
                    String uploadedImagePath = uploadMovieImage(posterFile, "poster", id);
                    if (uploadedImagePath != null) {
                        finalImagePath = uploadedImagePath;
                        System.out.println("Updated poster: " + finalImagePath);
                    }
                }

                if (bannerFile != null && !bannerFile.isEmpty()) {
                    String uploadedBannerPath = uploadMovieImage(bannerFile, "banner", id);
                    if (uploadedBannerPath != null) {
                        finalBannerPath = uploadedBannerPath;
                        System.out.println("Updated banner: " + finalBannerPath);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error uploading images: " + e.getMessage());
                redirectAttributes.addFlashAttribute("warning", "Lỗi upload ảnh: " + e.getMessage());
            }

            // Tạo movie object để update
            Movie movieToUpdate = new Movie();
            movieToUpdate.setMovieID(id);
            movieToUpdate.setMovieName(allParams.get("movieName").trim());
            movieToUpdate.setDescription(allParams.get("description").trim());
            movieToUpdate.setImage(finalImagePath);
            movieToUpdate.setBanner(finalBannerPath);
            movieToUpdate.setStudio(allParams.get("studio").trim());
            movieToUpdate.setDuration(Integer.parseInt(allParams.get("duration").trim()));
            movieToUpdate.setTrailer(allParams.get("trailer") != null && !allParams.get("trailer").trim().isEmpty() ?
                    allParams.get("trailer").trim() : null);

            // Giữ nguyên movieRate và startDate
            movieToUpdate.setMovieRate(existingMovie.getMovieRate());
            movieToUpdate.setStartDate(existingMovie.getStartDate());

            // Xử lý endDate
            String endDateStr = allParams.get("endDate");
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                try {
                    movieToUpdate.setEndDate(LocalDateTime.parse(endDateStr));
                } catch (Exception e) {
                    movieToUpdate.setEndDate(existingMovie.getEndDate());
                }
            } else {
                movieToUpdate.setEndDate(existingMovie.getEndDate());
            }

            movieToUpdate.setStatus(Movie_Status.valueOf(allParams.get("status")));

            // Update database - sử dụng MovieService thay vì direct method
            try {
                boolean updateSuccess = movieService.updateMovieComplete(id, movieToUpdate, genreIds, actorIds);

                if (updateSuccess) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật phim thành công!");
                    return "redirect:/admin/movies/" + id;
                } else {
                    redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật phim");
                    return "redirect:/admin/movies/" + id + "/edit";
                }
            } catch (IllegalArgumentException e) {
                // Xử lý lỗi validation từ MovieService
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/admin/movies/" + id + "/edit";
            }

        } catch (Exception e) {
            System.err.println("Error updating movie: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/movies/" + id + "/edit";
        }
    }

    // ==================== DIRECT UPDATE METHOD ====================

    @Transactional
    public boolean updateMovieDirectly(Integer movieId, Movie updatedMovie, List<Integer> genreIds, List<Integer> actorIds) {
        try {
            if (movieId == null || updatedMovie == null) {
                return false;
            }

            Movie existingMovie = movieRepository.findById(movieId).orElse(null);
            if (existingMovie == null) {
                return false;
            }

            // Cập nhật thông tin phim
            existingMovie.setMovieName(updatedMovie.getMovieName());
            existingMovie.setDescription(updatedMovie.getDescription());
            existingMovie.setImage(updatedMovie.getImage());
            existingMovie.setBanner(updatedMovie.getBanner());
            existingMovie.setStudio(updatedMovie.getStudio());
            existingMovie.setDuration(updatedMovie.getDuration());
            existingMovie.setTrailer(updatedMovie.getTrailer());
            existingMovie.setMovieRate(updatedMovie.getMovieRate());
            existingMovie.setStartDate(updatedMovie.getStartDate());
            existingMovie.setEndDate(updatedMovie.getEndDate());
            existingMovie.setStatus(updatedMovie.getStatus());

            // Cập nhật thể loại
            if (genreIds != null) {
                List<Genre> newGenres = new ArrayList<>();
                for (Integer genreId : genreIds) {
                    Genre genre = genreRepository.findById(genreId).orElse(null);
                    if (genre != null) {
                        newGenres.add(genre);
                    }
                }
                existingMovie.setGenres(newGenres);
            }

            // Cập nhật diễn viên
            if (actorIds != null) {
                List<Actor> newActors = new ArrayList<>();
                for (Integer actorId : actorIds) {
                    Actor actor = actorRepository.findById(actorId).orElse(null);
                    if (actor != null) {
                        newActors.add(actor);
                    }
                }
                existingMovie.setActors(newActors);
            }

            // Lưu vào database
            movieRepository.save(existingMovie);
            System.out.println("Movie updated successfully with images: " + existingMovie.getImage() + ", " + existingMovie.getBanner());
            return true;

        } catch (Exception e) {
            System.err.println("Error in updateMovieDirectly: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== API ENDPOINTS ====================

    /**
     * API endpoint to check if movie has booked seats
     * Used by frontend for status validation
     */
    @GetMapping("/{id}/check-booked-seats")
    @ResponseBody
    public Map<String, Object> checkBookedSeats(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("hasBookedSeats", movieService.hasBookedSeats(id));
            response.put("success", true);
        } catch (Exception e) {
            response.put("hasBookedSeats", false);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}