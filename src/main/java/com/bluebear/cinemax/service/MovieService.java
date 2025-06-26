package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private MovieActorRepository movieActorRepository;

    @Autowired
    private ActorService actorService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    // ==================== BASIC MOVIE OPERATIONS ====================

    /**
     * Lấy tất cả phim (bao gồm cả Active và Removed)
     */
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả phim active (cho public)
     */
    public List<MovieDTO> getAllActiveMovies() {
        return movieRepository.findByStatus(Movie.MovieStatus.Active).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả phim đã xóa (Removed)
     */
    public List<MovieDTO> getAllRemovedMovies() {
        return movieRepository.findByStatus(Movie.MovieStatus.Removed).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả phim active với phân trang
     */
    public Page<MovieDTO> getAllActiveMoviesWithPaging(PageRequest pageRequest) {
        Page<Movie> moviePage = movieRepository.findAll(pageRequest);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .filter(movie -> movie.getStatus() == Movie.MovieStatus.Active)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageRequest, moviePage.getTotalElements());
    }

    /**
     * Lấy phim đang chiếu
     */
    public List<MovieDTO> getNowShowingMovies() {
        return movieRepository.findActiveMovies(LocalDate.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim sắp chiếu
     */
    public List<MovieDTO> getUpcomingMovies() {
        return movieRepository.findUpcomingMovies(LocalDate.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim mới nhất
     */
    public List<MovieDTO> getRecentMovies(int limit) {
        List<Movie> movies = movieRepository.findByStatus(Movie.MovieStatus.Active);
        return movies.stream()
                .sorted((m1, m2) -> m2.getStartDate().compareTo(m1.getStartDate()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim theo ID (Integer version)
     */
    public MovieDTO getMovieById(Integer id) {
        if (id == null) return null;
        Optional<Movie> movie = movieRepository.findById(id);
        return movie.map(this::convertToDTO).orElse(null);
    }

    /**
     * Lấy phim theo ID (Long version)
     */
    public MovieDTO getMovieById(Long id) {
        if (id == null) return null;
        return getMovieById(id.intValue());
    }

    // ==================== SEARCH AND FILTER OPERATIONS ====================

    /**
     * Tìm kiếm phim theo keyword (tên) - tất cả phim
     */
    public List<MovieDTO> searchAllMoviesByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllMovies();
        }
        return movieRepository.findByMovieNameContainingIgnoreCase(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm phim theo keyword (tên) - chỉ phim active
     */
    public List<MovieDTO> searchMoviesByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveMovies();
        }
        return movieRepository.findByMovieNameContainingIgnoreCase(keyword).stream()
                .filter(movie -> movie.getStatus() == Movie.MovieStatus.Active)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm phim theo keyword và status
     */
    public List<MovieDTO> searchMoviesByKeywordAndStatus(String keyword, String status) {
        List<MovieDTO> movies;

        // Lọc theo status trước
        if ("Active".equals(status)) {
            movies = getAllActiveMovies();
        } else if ("Removed".equals(status)) {
            movies = getAllRemovedMovies();
        } else {
            movies = getAllMovies();
        }

        // Lọc theo keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            movies = movies.stream()
                    .filter(movie -> movie.getMovieName().toLowerCase()
                            .contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return movies;
    }

    /**
     * Tìm kiếm phim với phân trang
     */
    public Page<MovieDTO> searchMoviesByKeywordWithPaging(String keyword, PageRequest pageRequest) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveMoviesWithPaging(pageRequest);
        }

        List<Movie> movies = movieRepository.findByMovieNameContainingIgnoreCase(keyword);
        List<MovieDTO> filteredMovies = movies.stream()
                .filter(movie -> movie.getStatus() == Movie.MovieStatus.Active)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Tính toán phân trang thủ công
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredMovies.size());
        List<MovieDTO> pageContent = filteredMovies.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, filteredMovies.size());
    }

    /**
     * Alias cho searchMoviesByKeyword để tương thích với controller
     */
    public List<MovieDTO> searchMoviesByName(String keyword) {
        return searchMoviesByKeyword(keyword);
    }

    /**
     * Lấy phim theo thể loại (Integer version)
     */
    public List<MovieDTO> getMoviesByGenre(Integer genreId) {
        if (genreId == null) return getAllActiveMovies();
        return movieRepository.findByGenreId(genreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim theo thể loại (Long version)
     */
    public List<MovieDTO> getMoviesByGenre(Long genreId) {
        if (genreId == null) return getAllActiveMovies();
        return getMoviesByGenre(genreId.intValue());
    }

    /**
     * Lấy phim theo thể loại và status
     */
    public List<MovieDTO> getMoviesByGenreAndStatus(Integer genreId, String status) {
        List<MovieDTO> movies;

        if (genreId == null) {
            // Không có filter genre, lấy theo status
            if ("Active".equals(status)) {
                movies = getAllActiveMovies();
            } else if ("Removed".equals(status)) {
                movies = getAllRemovedMovies();
            } else {
                movies = getAllMovies();
            }
        } else {
            // Có filter genre
            movies = movieRepository.findByGenreId(genreId).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Lọc thêm theo status nếu cần
            if ("Active".equals(status)) {
                movies = movies.stream()
                        .filter(movie -> "Active".equals(movie.getStatus()))
                        .collect(Collectors.toList());
            } else if ("Removed".equals(status)) {
                movies = movies.stream()
                        .filter(movie -> "Removed".equals(movie.getStatus()))
                        .collect(Collectors.toList());
            }
        }

        return movies;
    }

    /**
     * Lấy phim theo thể loại với phân trang
     */
    public Page<MovieDTO> getMoviesByGenreWithPaging(Long genreId, PageRequest pageRequest) {
        if (genreId == null) {
            return getAllActiveMoviesWithPaging(pageRequest);
        }

        List<Movie> movies = movieRepository.findByGenreId(genreId.intValue());
        List<MovieDTO> movieDTOs = movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Tính toán phân trang thủ công
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), movieDTOs.size());
        List<MovieDTO> pageContent = movieDTOs.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, movieDTOs.size());
    }

    /**
     * Lấy phim theo diễn viên
     */
    public List<MovieDTO> getMoviesByActor(Integer actorId) {
        if (actorId == null) return getAllActiveMovies();
        return movieRepository.findByActorId(actorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy top phim theo rating
     */
    public List<MovieDTO> getTopRatedMovies() {
        return movieRepository.findTop10ByStatusOrderByMovieRateDesc(Movie.MovieStatus.Active).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim liên quan (cùng thể loại, trừ phim hiện tại)
     */
    public List<MovieDTO> getRelatedMovies(Long movieId) {
        if (movieId == null) return List.of();

        Movie currentMovie = movieRepository.findById(movieId.intValue()).orElse(null);
        if (currentMovie == null || currentMovie.getMovieGenres() == null || currentMovie.getMovieGenres().isEmpty()) {
            return List.of();
        }

        // Lấy genreId đầu tiên của phim hiện tại
        Integer genreId = currentMovie.getMovieGenres().iterator().next().getGenre().getGenreId();

        return movieRepository.findByGenreId(genreId).stream()
                .filter(movie -> !movie.getMovieId().equals(movieId.intValue())) // Loại trừ phim hiện tại
                .filter(movie -> movie.getStatus() == Movie.MovieStatus.Active)
                .limit(4)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim theo studio
     */
    public List<MovieDTO> getMoviesByStudio(String studio) {
        if (studio == null || studio.trim().isEmpty()) {
            return getAllActiveMovies();
        }

        return movieRepository.findByStudioContainingIgnoreCaseAndStatus(studio, Movie.MovieStatus.Active)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== EXISTENCE CHECK OPERATIONS ====================

    /**
     * Kiểm tra phim có tồn tại không
     */
    public boolean existsById(Integer id) {
        if (id == null) return false;
        return movieRepository.existsById(id);
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
        return existsById(id.intValue());
    }

    /**
     * Kiểm tra tên phim có bị trùng không (trừ phim hiện tại)
     */
    public boolean isMovieNameExists(String movieName, Integer excludeId) {
        if (movieName == null || movieName.trim().isEmpty()) return false;

        List<Movie> movies = movieRepository.findByMovieNameContainingIgnoreCase(movieName.trim());

        // Loại bỏ phim hiện tại khỏi kiểm tra
        if (excludeId != null) {
            movies = movies.stream()
                    .filter(movie -> !movie.getMovieId().equals(excludeId))
                    .collect(Collectors.toList());
        }

        // Kiểm tra tên chính xác
        return movies.stream()
                .anyMatch(movie -> movie.getMovieName().equalsIgnoreCase(movieName.trim()));
    }

    /**
     * Validate tên phim có unique không (cho add movie)
     */
    public boolean isMovieNameUniqueForAdd(String movieName) {
        if (movieName == null || movieName.trim().isEmpty()) {
            return false;
        }

        return !isMovieNameExists(movieName.trim(), null);
    }

    // ==================== STATISTICS OPERATIONS ====================

    /**
     * Các phương thức thống kê
     */
    public long countAllMovies() {
        return movieRepository.count();
    }

    public long countActiveMovies() {
        return movieRepository.findByStatus(Movie.MovieStatus.Active).size();
    }

    public long countNowShowingMovies() {
        return movieRepository.findActiveMovies(LocalDate.now()).size();
    }

    public long countUpcomingMovies() {
        return movieRepository.findUpcomingMovies(LocalDate.now()).size();
    }

    public double getAverageRating() {
        List<Movie> activeMovies = movieRepository.findByStatus(Movie.MovieStatus.Active);
        return activeMovies.stream()
                .filter(movie -> movie.getMovieRate() != null)
                .mapToDouble(movie -> movie.getMovieRate().doubleValue())
                .average()
                .orElse(0.0);
    }

    /**
     * Lấy phim được thêm gần đây nhất
     */
    public List<MovieDTO> getRecentlyAddedMovies(int limit) {
        // Sử dụng movieId để sort vì ID tăng dần theo thời gian thêm
        return movieRepository.findByStatus(Movie.MovieStatus.Active)
                .stream()
                .sorted((m1, m2) -> m2.getMovieId().compareTo(m1.getMovieId()))
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Thống kê phim theo tháng được thêm
     */
    public Map<String, Long> getMovieCountByMonth() {
        // TODO: Implement this if needed for dashboard
        // Có thể thêm field createdDate vào Movie entity để track
        return new HashMap<>();
    }

    // ==================== ADD MOVIE OPERATIONS ====================

    /**
     * Validate dữ liệu phim trước khi thêm - ULTRA RELAXED VERSION
     */
    public String validateMovieDataForAdd(Movie movie) {
        if (movie == null) {
            return "Dữ liệu phim không hợp lệ";
        }

        // Kiểm tra tên phim - CHỈ CÓ ĐIỀU NÀY LÀ BẮT BUỘC
        if (movie.getMovieName() == null || movie.getMovieName().trim().isEmpty()) {
            return "Tên phim không được để trống";
        }

        if (movie.getMovieName().length() > 100) {
            return "Tên phim không được vượt quá 100 ký tự";
        }

        // Kiểm tra tên phim trùng lặp
        if (isMovieNameExists(movie.getMovieName(), null)) {
            return "Tên phim đã tồn tại";
        }

        // TẤT CẢ CÁC VALIDATION KHÁC CHỈ KIỂM TRA NẾU CÓ GIÁ TRỊ

        // Kiểm tra thời lượng - CHỈ KIỂM TRA NẾU KHÔNG NULL
        if (movie.getDuration() != null) {
            if (movie.getDuration() <= 0) {
                return "Thời lượng phim phải lớn hơn 0";
            }
            if (movie.getDuration() > 500) {
                return "Thời lượng phim không được vượt quá 500 phút";
            }
        }

        // Kiểm tra ngày - CHỈ KIỂM TRA NẾU CẢ HAI KHÔNG NULL
        if (movie.getStartDate() != null && movie.getEndDate() != null) {
            if (movie.getStartDate().isAfter(movie.getEndDate())) {
                return "Ngày bắt đầu chiếu phải trước ngày kết thúc";
            }
        }

        // Kiểm tra rating - CHỈ KIỂM TRA NẾU KHÔNG NULL
        if (movie.getMovieRate() != null) {
            try {
                BigDecimal normalizedRating = movie.getMovieRate().setScale(1, RoundingMode.HALF_UP);
                movie.setMovieRate(normalizedRating);

                if (normalizedRating.compareTo(BigDecimal.ZERO) < 0 ||
                        normalizedRating.compareTo(new BigDecimal("5.0")) > 0) {
                    return "Đánh giá phải từ 0.0 đến 5.0";
                }
            } catch (Exception e) {
                return "Định dạng đánh giá không hợp lệ";
            }
        }

        // Kiểm tra mô tả - CHỈ KIỂM TRA NẾU KHÔNG NULL
        if (movie.getDescription() != null && movie.getDescription().length() > 1000) {
            return "Mô tả không được vượt quá 1000 ký tự";
        }

        // Kiểm tra studio - CHỈ KIỂM TRA NẾU KHÔNG NULL
        if (movie.getStudio() != null && movie.getStudio().length() > 50) {
            return "Tên studio không được vượt quá 50 ký tự";
        }

        // Kiểm tra URL fields - CHỈ KIỂM TRA NẾU KHÔNG NULL
        if (movie.getImage() != null && !movie.getImage().trim().isEmpty()) {
            if (movie.getImage().length() > 500) {
                return "URL hình ảnh quá dài";
            }
        }

        if (movie.getBanner() != null && !movie.getBanner().trim().isEmpty()) {
            if (movie.getBanner().length() > 500) {
                return "URL banner quá dài";
            }
        }

        if (movie.getTrailer() != null && !movie.getTrailer().trim().isEmpty()) {
            if (movie.getTrailer().length() > 500) {
                return "URL trailer quá dài";
            }
        }

        System.out.println("✅ Validation passed for movie: " + movie.getMovieName());
        return null; // Không có lỗi
    }

    /**
     * Validate genres cho ADD - RELAXED VERSION
     */
    public String validateGenresForAdd(List<Integer> genreIds) {
        // CHO PHÉP NULL hoặc EMPTY cho phim đang chuẩn bị
        if (genreIds == null || genreIds.isEmpty()) {
            System.out.println("Warning: Movie added without genres");
            return null;
        }

        // Kiểm tra tất cả genreIds có tồn tại không
        for (Integer genreId : genreIds) {
            if (genreId == null || !genreRepository.existsById(genreId)) {
                return "Genre ID " + genreId + " không tồn tại";
            }
        }

        // Kiểm tra số lượng genres (tối đa 5)
        if (genreIds.size() > 5) {
            return "Một phim chỉ có thể có tối đa 5 thể loại";
        }

        return null; // Không có lỗi
    }

    /**
     * Validate actors cho ADD - RELAXED VERSION
     */
    public String validateActorsForAdd(List<Integer> actorIds) {
        // CHO PHÉP NULL hoặc EMPTY
        if (actorIds == null || actorIds.isEmpty()) {
            System.out.println("Warning: Movie added without actors");
            return null;
        }

        // Kiểm tra tất cả actorIds có tồn tại không
        for (Integer actorId : actorIds) {
            if (actorId == null) {
                return "Actor ID không được null";
            }

            if (!actorRepository.existsById(actorId)) {
                return "Actor ID " + actorId + " không tồn tại";
            }
        }

        // Kiểm tra số lượng actors (tối đa 20)
        if (actorIds.size() > 20) {
            return "Một phim chỉ có thể có tối đa 20 diễn viên";
        }

        return null; // Không có lỗi
    }

    /**
     * Set default values cho các field null khi thêm phim mới
     */
    private void setDefaultValuesForNewMovie(Movie movie) {
        System.out.println("=== SETTING DEFAULT VALUES ===");

        // Set default duration nếu null
        if (movie.getDuration() == null) {
            movie.setDuration(0);
            System.out.println("Set default duration: 120 minutes");
        }

        // Set default dates nếu null
        if (movie.getStartDate() == null) {
            movie.setStartDate(LocalDate.now()); //  now
            System.out.println("Set default start date: " + movie.getStartDate());
        }

        if (movie.getEndDate() == null) {
            movie.setEndDate(movie.getStartDate().plusDays(30)); // 30 days showing period
            System.out.println("Set default end date: " + movie.getEndDate());
        }

        // Set default status nếu null
        if (movie.getStatus() == null) {
            movie.setStatus(Movie.MovieStatus.Active);
            System.out.println("Set default status: Active");
        }

        // Set default rating nếu null
        if (movie.getMovieRate() == null) {
            movie.setMovieRate(new BigDecimal("0.0").setScale(1, RoundingMode.HALF_UP));
            System.out.println("Set default rating: 0.0");
        }

        // Handle string fields với default values cho NOT NULL constraints
        if (movie.getMovieName() != null) {
            movie.setMovieName(movie.getMovieName().trim());
        }

        // Set default cho description nếu null (có thể NOT NULL trong DB)
        if (movie.getDescription() == null || movie.getDescription().trim().isEmpty()) {
            movie.setDescription("Đang cập nhật thông tin phim...");
            System.out.println("Set default description");
        } else {
            movie.setDescription(movie.getDescription().trim());
        }

        // Set default cho studio nếu null (có thể NOT NULL trong DB)
        if (movie.getStudio() == null || movie.getStudio().trim().isEmpty()) {
            movie.setStudio("Chưa xác định");
            System.out.println("Set default studio");
        } else {
            movie.setStudio(movie.getStudio().trim());
        }

        // Set default cho image nếu null (có thể NOT NULL trong DB)
        if (movie.getImage() == null || movie.getImage().trim().isEmpty()) {
            movie.setImage("/images/default-poster.jpg");
            System.out.println("Set default image");
        } else {
            movie.setImage(movie.getImage().trim());
        }

        // Set default cho banner nếu null (NOT NULL constraint)
        if (movie.getBanner() == null || movie.getBanner().trim().isEmpty()) {
            movie.setBanner("/images/default-banner.jpg");
            System.out.println("Set default banner");
        } else {
            movie.setBanner(movie.getBanner().trim());
        }

        // Set default cho trailer nếu null (có thể NOT NULL trong DB)
        if (movie.getTrailer() == null || movie.getTrailer().trim().isEmpty()) {
            movie.setTrailer("https://www.youtube.com/watch?v=dQw4w9WgXcQ"); // Default placeholder
            System.out.println("Set default trailer");
        } else {
            movie.setTrailer(movie.getTrailer().trim());
        }

        System.out.println("=== DEFAULT VALUES SET ===");
        System.out.println("Final Movie Data:");
        System.out.println("- Name: " + movie.getMovieName());
        System.out.println("- Description: " + movie.getDescription());
        System.out.println("- Studio: " + movie.getStudio());
        System.out.println("- Duration: " + movie.getDuration());
        System.out.println("- Rating: " + movie.getMovieRate());
        System.out.println("- Image: " + movie.getImage());
        System.out.println("- Banner: " + movie.getBanner());
        System.out.println("- Trailer: " + movie.getTrailer());
        System.out.println("- Start Date: " + movie.getStartDate());
        System.out.println("- End Date: " + movie.getEndDate());
        System.out.println("- Status: " + movie.getStatus());
    }

    /**
     * Thêm phim mới với validation RELAXED
     */
    @Transactional
    public MovieDTO addMovie(Movie movie, List<Integer> genreIds, List<Integer> actorIds) {
        if (movie == null) {
            throw new IllegalArgumentException("Dữ liệu phim không được null");
        }

        try {
            System.out.println("=== ADD MOVIE SERVICE START ===");
            System.out.println("Movie name: " + movie.getMovieName());

            // 1. Validate dữ liệu phim với RELAXED rules
            String validationError = validateMovieDataForAdd(movie);
            if (validationError != null) {
                throw new IllegalArgumentException(validationError);
            }

            // 2. Validate genres với RELAXED rules
            String genreValidationError = validateGenresForAdd(genreIds);
            if (genreValidationError != null) {
                throw new IllegalArgumentException(genreValidationError);
            }

            // 3. Validate actors với RELAXED rules
            String actorValidationError = validateActorsForAdd(actorIds);
            if (actorValidationError != null) {
                throw new IllegalArgumentException(actorValidationError);
            }

            // 4. Set default values for NULL fields
            setDefaultValuesForNewMovie(movie);

            // 5. Lưu movie trước
            Movie savedMovie = movieRepository.save(movie);
            System.out.println("Movie saved with ID: " + savedMovie.getMovieId());

            // 6. Thêm genres (nếu có)
            if (genreIds != null && !genreIds.isEmpty()) {
                addGenresToMovie(savedMovie, genreIds);
                System.out.println("Added " + genreIds.size() + " genres to movie");
            } else {
                System.out.println("No genres added to movie");
            }

            // 7. Thêm actors (nếu có)
            if (actorIds != null && !actorIds.isEmpty()) {
                addActorsToMovie(savedMovie, actorIds);
                System.out.println("Added " + actorIds.size() + " actors to movie");
            } else {
                System.out.println("No actors added to movie");
            }

            // 8. Refresh movie entity để có đầy đủ thông tin
            Movie refreshedMovie = movieRepository.findById(savedMovie.getMovieId()).orElse(null);
            if (refreshedMovie == null) {
                throw new RuntimeException("Không thể tải lại thông tin phim sau khi lưu");
            }

            MovieDTO result = convertToDTO(refreshedMovie);
            System.out.println("=== ADD MOVIE SERVICE SUCCESS ===");
            return result;

        } catch (Exception e) {
            System.err.println("Error in addMovie: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Có lỗi xảy ra khi thêm phim: " + e.getMessage());
        }
    }

    /**
     * Thêm genres cho phim mới
     */
    @Transactional
    public void addGenresToMovie(Movie movie, List<Integer> genreIds) {
        if (movie == null || genreIds == null || genreIds.isEmpty()) {
            return;
        }

        try {
            Set<MovieGenre> movieGenres = new HashSet<>();

            for (Integer genreId : genreIds) {
                Genre genre = genreRepository.findById(genreId).orElse(null);
                if (genre != null) {
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setMovie(movie);
                    movieGenre.setGenre(genre);
                    movieGenres.add(movieGenre);
                } else {
                    System.err.println("Genre not found: " + genreId);
                }
            }

            if (!movieGenres.isEmpty()) {
                movieGenreRepository.saveAll(movieGenres);
                System.out.println("Saved " + movieGenres.size() + " movie-genre relationships");
            }

        } catch (Exception e) {
            System.err.println("Error adding genres to movie: " + e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi thêm thể loại cho phim: " + e.getMessage());
        }
    }

    /**
     * Thêm actors cho phim mới
     */
    @Transactional
    public void addActorsToMovie(Movie movie, List<Integer> actorIds) {
        if (movie == null || actorIds == null || actorIds.isEmpty()) {
            return;
        }

        try {
            Set<MovieActor> movieActors = new HashSet<>();

            for (Integer actorId : actorIds) {
                Actor actor = actorRepository.findById(actorId).orElse(null);
                if (actor != null) {
                    MovieActor movieActor = new MovieActor();
                    movieActor.setMovie(movie);
                    movieActor.setActor(actor);
                    movieActors.add(movieActor);
                } else {
                    System.err.println("Actor not found: " + actorId);
                }
            }

            if (!movieActors.isEmpty()) {
                movieActorRepository.saveAll(movieActors);
                System.out.println("Saved " + movieActors.size() + " movie-actor relationships");
            }

        } catch (Exception e) {
            System.err.println("Error adding actors to movie: " + e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi thêm diễn viên cho phim: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem có thể thêm phim mới không
     */
    public boolean canAddMovie() {
        try {
            // Kiểm tra có ít nhất 1 genre trong hệ thống
            long genreCount = genreRepository.count();
            if (genreCount == 0) {
                return false;
            }

            // Có thể thêm thêm các kiểm tra khác nếu cần
            return true;
        } catch (Exception e) {
            System.err.println("Error checking if can add movie: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy thống kê để hiển thị trên form add
     */
    public Map<String, Object> getAddMovieStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Sử dụng repository directly để tránh dependency issue
            stats.put("totalGenres", genreRepository.count());
            stats.put("totalActors", actorRepository.count());
            stats.put("totalMovies", countAllMovies());
            stats.put("canAdd", canAddMovie());
        } catch (Exception e) {
            System.err.println("Error getting add movie stats: " + e.getMessage());
            stats.put("canAdd", false);
        }

        return stats;
    }

    /**
     * Tạo phim mẫu để test
     */
    public Movie createSampleMovie() {
        Movie sample = new Movie();
        sample.setMovieName("Sample Movie");
        sample.setDescription("This is a sample movie for testing");
        sample.setDuration(120);
        sample.setMovieRate(new BigDecimal("4.5").setScale(1, RoundingMode.HALF_UP));
        sample.setStartDate(LocalDate.now());
        sample.setEndDate(LocalDate.now().plusDays(30));
        sample.setStatus(Movie.MovieStatus.Active);
        sample.setStudio("Sample Studio");
        sample.setImage("/images/default-poster.jpg");
        sample.setBanner("/images/default-banner.jpg");
        sample.setTrailer("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

        return sample;
    }

    /**
     * Tạo phim tối giản chỉ với tên - FOR TESTING MINIMAL ADD
     */
    public Movie createMinimalMovie(String movieName) {
        Movie minimal = new Movie();
        minimal.setMovieName(movieName);
        // Tất cả các field khác sẽ là null và được set default values
        return minimal;
    }

    /**
     * Safe method để tạo movie entity từ minimal data
     */
    public Movie createMovieFromMinimalData(String movieName) {
        if (movieName == null || movieName.trim().isEmpty()) {
            throw new IllegalArgumentException("Movie name cannot be null or empty");
        }

        Movie movie = new Movie();
        movie.setMovieName(movieName.trim());

        // Set all other fields to null - will be handled by setDefaultValuesForNewMovie
        movie.setDescription(null);
        movie.setImage(null);
        movie.setBanner(null);
        movie.setStudio(null);
        movie.setDuration(null);
        movie.setTrailer(null);
        movie.setMovieRate(null);
        movie.setStartDate(null);
        movie.setEndDate(null);
        movie.setStatus(null);

        return movie;
    }

    // ==================== UPDATE MOVIE OPERATIONS ====================

    /**
     * Validate dữ liệu phim trước khi cập nhật - STANDARD VERSION
     */
    public String validateMovieData(Movie movie, Integer excludeId) {
        if (movie == null) {
            return "Dữ liệu phim không hợp lệ";
        }

        // Kiểm tra tên phim
        if (movie.getMovieName() == null || movie.getMovieName().trim().isEmpty()) {
            return "Tên phim không được để trống";
        }

        if (movie.getMovieName().length() > 100) {
            return "Tên phim không được vượt quá 100 ký tự";
        }

        // Kiểm tra tên phim trùng lặp
        if (isMovieNameExists(movie.getMovieName(), excludeId)) {
            return "Tên phim đã tồn tại";
        }

        // Kiểm tra thời lượng
        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            return "Thời lượng phim phải lớn hơn 0";
        }

        if (movie.getDuration() > 500) {
            return "Thời lượng phim không được vượt quá 500 phút";
        }

        // Kiểm tra ngày
        if (movie.getStartDate() == null) {
            return "Ngày bắt đầu chiếu không được để trống";
        }

        if (movie.getEndDate() == null) {
            return "Ngày kết thúc chiếu không được để trống";
        }

        if (movie.getStartDate().isAfter(movie.getEndDate())) {
            return "Ngày bắt đầu chiếu phải trước ngày kết thúc";
        }

        // Kiểm tra rating
        if (movie.getMovieRate() != null) {
            BigDecimal normalizedRating = movie.getMovieRate().setScale(1, RoundingMode.HALF_UP);
            movie.setMovieRate(normalizedRating);

            if (normalizedRating.compareTo(BigDecimal.ZERO) < 0 ||
                    normalizedRating.compareTo(new BigDecimal("5.0")) > 0) {
                return "Đánh giá phải từ 0.0 đến 5.0";
            }

            System.out.println("Validated and normalized rating: " + normalizedRating);
        }

        // Kiểm tra mô tả
        if (movie.getDescription() != null && movie.getDescription().length() > 1000) {
            return "Mô tả không được vượt quá 1000 ký tự";
        }

        // Kiểm tra studio
        if (movie.getStudio() != null && movie.getStudio().length() > 50) {
            return "Tên studio không được vượt quá 50 ký tự";
        }

        return null; // Không có lỗi
    }

    /**
     * Validate genres trước khi cập nhật
     */
    public String validateGenres(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return null; // Cho phép không có genre
        }

        // Kiểm tra tất cả genreIds có tồn tại không
        for (Integer genreId : genreIds) {
            if (genreId == null || !genreRepository.existsById(genreId)) {
                return "Genre ID " + genreId + " không tồn tại";
            }
        }

        // Kiểm tra số lượng genres (tối đa 5)
        if (genreIds.size() > 5) {
            return "Một phim chỉ có thể có tối đa 5 thể loại";
        }

        return null; // Không có lỗi
    }

    /**
     * Validate thông tin actors - FIXED VERSION
     */
    public String validateActors(List<Integer> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return null; // Cho phép không có actor
        }

        // Kiểm tra tất cả actorIds có tồn tại không
        for (Integer actorId : actorIds) {
            if (actorId == null) {
                return "Actor ID không được null";
            }

            // Sử dụng repository thay vì service để tránh circular dependency
            if (!actorRepository.existsById(actorId)) {
                return "Actor ID " + actorId + " không tồn tại";
            }
        }

        // Kiểm tra số lượng actors (tối đa 20)
        if (actorIds.size() > 20) {
            return "Một phim chỉ có thể có tối đa 20 diễn viên";
        }

        return null; // Không có lỗi
    }

    /**
     * Cập nhật thông tin cơ bản của phim - FIXED VERSION
     */
    private void updateBasicMovieInfo(Movie movie, Movie movieDetails) {
        System.out.println("=== UPDATE BASIC INFO DEBUG ===");
        System.out.println("Current movie rating: " + movie.getMovieRate());
        System.out.println("New movie rating from form: " + movieDetails.getMovieRate());

        movie.setMovieName(movieDetails.getMovieName());
        movie.setDescription(movieDetails.getDescription());
        movie.setImage(movieDetails.getImage());
        movie.setBanner(movieDetails.getBanner());
        movie.setStudio(movieDetails.getStudio());
        movie.setDuration(movieDetails.getDuration());
        movie.setTrailer(movieDetails.getTrailer());

        // ⚠️ FIX CRITICAL: Kiểm tra null trước khi set rating
        if (movieDetails.getMovieRate() != null) {
            BigDecimal newRating = movieDetails.getMovieRate().setScale(1, RoundingMode.HALF_UP);
            System.out.println("Setting new rating (with scale): " + newRating);
            movie.setMovieRate(newRating);
        } else {
            System.out.println("WARNING: movieDetails.getMovieRate() is NULL - keeping old rating: " + movie.getMovieRate());
            // Không thay đổi rating nếu null
        }

        movie.setStartDate(movieDetails.getStartDate());
        movie.setEndDate(movieDetails.getEndDate());
        movie.setStatus(movieDetails.getStatus());

        System.out.println("Final movie rating after basic update: " + movie.getMovieRate());
        System.out.println("=== END UPDATE BASIC INFO DEBUG ===");
    }

    /**
     * Debug method để kiểm tra rating flow
     */
    public void debugMovieRating(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie != null) {
            System.out.println("=== MOVIE RATING DEBUG ===");
            System.out.println("Movie ID: " + movie.getMovieId());
            System.out.println("Movie Name: " + movie.getMovieName());
            System.out.println("Current Rating: " + movie.getMovieRate());
            System.out.println("Rating Class: " + (movie.getMovieRate() != null ? movie.getMovieRate().getClass() : "null"));
            System.out.println("Rating Scale: " + (movie.getMovieRate() != null ? movie.getMovieRate().scale() : "null"));
            System.out.println("Rating Precision: " + (movie.getMovieRate() != null ? movie.getMovieRate().precision() : "null"));
            System.out.println("=========================");
        }
    }

    /**
     * Cập nhật genres của phim - IMPROVED VERSION
     */
    @Transactional
    public void updateMovieGenresImproved(Movie movie, List<Integer> genreIds) {
        if (movie == null) return;

        try {
            // 1. Xóa tất cả MovieGenre hiện tại cho phim này
            movieGenreRepository.deleteByMovieId(movie.getMovieId());

            // 2. Flush để đảm bảo delete được thực hiện
            movieGenreRepository.flush();

            // 3. Thêm genres mới
            if (genreIds != null && !genreIds.isEmpty()) {
                Set<MovieGenre> newMovieGenres = new HashSet<>();

                for (Integer genreId : genreIds) {
                    Genre genre = genreRepository.findById(genreId).orElse(null);
                    if (genre != null) {
                        MovieGenre movieGenre = new MovieGenre();
                        movieGenre.setMovie(movie);
                        movieGenre.setGenre(genre);
                        newMovieGenres.add(movieGenre);
                    }
                }

                // 4. Lưu tất cả MovieGenre mới
                if (!newMovieGenres.isEmpty()) {
                    movieGenreRepository.saveAll(newMovieGenres);
                }
            }

            // 5. Refresh movie entity để có genres mới
            movie.setMovieGenres(getMovieGenresByMovieId(movie.getMovieId()));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating movie genres: " + e.getMessage());
        }
    }

    /**
     * Cập nhật phim hoàn chỉnh bao gồm cả genres - IMPROVED VERSION
     */
    @Transactional
    public boolean updateMovieComplete(Integer id, Movie movieDetails, List<Integer> genreIds) {
        if (id == null || movieDetails == null) return false;

        try {
            Optional<Movie> optionalMovie = movieRepository.findById(id);
            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();

                // Debug rating trước update
                System.out.println("=== BEFORE UPDATE DEBUG ===");
                debugMovieRating(id);

                // 1. Cập nhật các thuộc tính cơ bản
                updateBasicMovieInfo(movie, movieDetails);

                // 2. Lưu movie trước (để có ID cho relationships)
                Movie savedMovie = movieRepository.save(movie);

                // Debug rating sau update
                System.out.println("=== AFTER UPDATE DEBUG ===");
                debugMovieRating(id);

                // 3. Cập nhật genres với transaction
                updateMovieGenresImproved(savedMovie, genreIds);

                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating movie: " + e.getMessage());
        }
    }

    /**
     * Cập nhật phim với validation đầy đủ
     */
    @Transactional
    public MovieDTO updateMovieWithFullValidation(Integer id, Movie movieDetails, List<Integer> genreIds) {
        if (id == null || movieDetails == null) return null;

        // Validate movie data
        String movieValidationError = validateMovieData(movieDetails, id);
        if (movieValidationError != null) {
            throw new IllegalArgumentException(movieValidationError);
        }

        // Validate genres
        String genreValidationError = validateGenres(genreIds);
        if (genreValidationError != null) {
            throw new IllegalArgumentException(genreValidationError);
        }

        // Cập nhật
        boolean success = updateMovieComplete(id, movieDetails, genreIds);
        if (success) {
            return getMovieById(id);
        }

        return null;
    }

    /**
     * Cập nhật phim với validation
     */
    public MovieDTO updateMovieWithValidation(Integer id, Movie movieDetails, List<Integer> genreIds) {
        if (id == null || movieDetails == null) return null;

        // Validate dữ liệu
        String validationError = validateMovieData(movieDetails, id);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Cập nhật
        boolean success = updateMovieComplete(id, movieDetails, genreIds);
        if (success) {
            return getMovieById(id);
        }

        return null;
    }

    /**
     * Cập nhật ngày hết hạn chiếu của phim
     */
    @Transactional
    public boolean updateEndDate(Integer id, LocalDate endDate) {
        // Validate input parameters
        if (id == null || endDate == null) {
            System.err.println("UpdateEndDate: Invalid parameters - ID or endDate is null");
            return false;
        }

        try {
            // Tìm phim theo ID
            Optional<Movie> optionalMovie = movieRepository.findById(id);
            if (!optionalMovie.isPresent()) {
                System.err.println("UpdateEndDate: Movie not found with ID: " + id);
                return false;
            }

            Movie movie = optionalMovie.get();

            // Validate business logic - endDate phải sau startDate
            if (movie.getStartDate() != null && endDate.isBefore(movie.getStartDate())) {
                System.err.println("UpdateEndDate: End date cannot be before start date");
                throw new IllegalArgumentException("Ngày kết thúc không thể trước ngày bắt đầu chiếu");
            }

            // Validate endDate không được ở quá khứ (nếu phim đang active)
            if (movie.getStatus() == Movie.MovieStatus.Active && endDate.isBefore(LocalDate.now())) {
                System.err.println("UpdateEndDate: End date cannot be in the past for active movies");
                throw new IllegalArgumentException("Ngày kết thúc không thể ở quá khứ đối với phim đang chiếu");
            }

            // Log thông tin cập nhật
            System.out.println("UpdateEndDate: Updating movie '" + movie.getMovieName() + "' (ID: " + id + ")");
            System.out.println("Old end date: " + movie.getEndDate());
            System.out.println("New end date: " + endDate);

            // Cập nhật ngày kết thúc
            movie.setEndDate(endDate);

            // Lưu vào database
            Movie savedMovie = movieRepository.save(movie);

            // Verify save operation
            if (savedMovie != null && endDate.equals(savedMovie.getEndDate())) {
                System.out.println("UpdateEndDate: Successfully updated end date for movie ID: " + id);
                return true;
            } else {
                System.err.println("UpdateEndDate: Failed to verify save operation");
                return false;
            }

        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            System.err.println("UpdateEndDate: Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Có lỗi xảy ra khi cập nhật ngày hết hạn chiếu: " + e.getMessage());
        }
    }

    /**
     * Cập nhật ngày hết hạn chiếu của phim (alias method)
     */
    public boolean updateMovieEndDate(Integer id, LocalDate newEndDate) {
        return updateEndDate(id, newEndDate);
    }

    // ==================== GENRE MANAGEMENT OPERATIONS ====================

    /**
     * Lấy MovieGenres theo movieId
     */
    private Set<MovieGenre> getMovieGenresByMovieId(Integer movieId) {
        return new HashSet<>(movieGenreRepository.findByMovieId(movieId));
    }

    /**
     * Lấy Genre IDs hiện tại của phim
     */
    public List<Integer> getCurrentMovieGenreIds(Integer movieId) {
        if (movieId == null) return new ArrayList<>();

        return movieGenreRepository.findByMovieId(movieId).stream()
                .map(mg -> mg.getGenre().getGenreId())
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra phim có thuộc genre không
     */
    public boolean movieHasGenre(Integer movieId, Integer genreId) {
        if (movieId == null || genreId == null) return false;

        return movieGenreRepository.existsByMovieIdAndGenreId(movieId, genreId);
    }

    /**
     * Thêm một genre cho phim
     */
    @Transactional
    public boolean addGenreToMovie(Integer movieId, Integer genreId) {
        if (movieId == null || genreId == null) return false;

        // Kiểm tra đã tồn tại chưa
        if (movieHasGenre(movieId, genreId)) {
            return true; // Đã tồn tại rồi
        }

        try {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            Genre genre = genreRepository.findById(genreId).orElse(null);

            if (movie != null && genre != null) {
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovie(movie);
                movieGenre.setGenre(genre);
                movieGenreRepository.save(movieGenre);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Xóa một genre khỏi phim
     */
    @Transactional
    public boolean removeGenreFromMovie(Integer movieId, Integer genreId) {
        if (movieId == null || genreId == null) return false;

        try {
            movieGenreRepository.deleteByMovieIdAndGenreId(movieId, genreId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy genres từ phim này sang phim khác
     */
    @Transactional
    public boolean copyGenres(Integer fromMovieId, Integer toMovieId) {
        if (fromMovieId == null || toMovieId == null) return false;

        try {
            List<Integer> genreIds = getCurrentMovieGenreIds(fromMovieId);
            Movie toMovie = movieRepository.findById(toMovieId).orElse(null);

            if (toMovie != null) {
                updateMovieGenresImproved(toMovie, genreIds);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Đếm số phim theo genre
     */
    public long countMoviesByGenre(Integer genreId) {
        if (genreId == null) return 0;
        return movieGenreRepository.countByGenreId(genreId);
    }

    /**
     * Lấy tất cả genres của một phim
     */
    public List<Genre> getGenresByMovieId(Integer movieId) {
        if (movieId == null) return new ArrayList<>();

        return movieGenreRepository.findByMovieId(movieId).stream()
                .map(MovieGenre::getGenre)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Xóa phim (soft delete - chuyển status thành Removed)
     */
    public boolean deleteMovie(Integer id) {
        if (id == null) return false;

        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            movie.setStatus(Movie.MovieStatus.Removed);
            movieRepository.save(movie);
            return true;
        }
        return false;
    }

    /**
     * Xóa phim hoàn toàn
     */
    public boolean hardDeleteMovie(Integer id) {
        if (id == null) return false;

        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ==================== UTILITY OPERATIONS ====================

    /**
     * Convert Entity to DTO
     */
    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) return null;

        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());
        dto.setMovieRate(movie.getMovieRate());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setStatus(movie.getStatus() != null ? movie.getStatus().toString() : "Unknown");

        // Lấy danh sách thể loại - null check
        if (movie.getMovieGenres() != null && !movie.getMovieGenres().isEmpty()) {
            List<String> genres = movie.getMovieGenres().stream()
                    .filter(mg -> mg.getGenre() != null)
                    .map(mg -> mg.getGenre().getGenreName())
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            dto.setGenres(genres);
        }

        // Lấy danh sách diễn viên - null check
        if (movie.getMovieActors() != null && !movie.getMovieActors().isEmpty()) {
            List<String> actors = movie.getMovieActors().stream()
                    .filter(ma -> ma.getActor() != null)
                    .map(ma -> ma.getActor().getActorName())
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            dto.setActors(actors);
        }

        return dto;
    }

    /**
     * Lấy lịch sử thay đổi của phim (có thể mở rộng sau)
     */
    public List<String> getMovieUpdateHistory(Integer movieId) {
        // TODO: Implement movie update history tracking
        // Có thể tạo bảng movie_history để track changes
        return new ArrayList<>();
    }

    /**
     * Backup dữ liệu phim trước khi update
     */
    public MovieDTO backupMovieBeforeUpdate(Integer movieId) {
        if (movieId == null) return null;

        MovieDTO currentMovie = getMovieById(movieId);
        if (currentMovie != null) {
            // TODO: Lưu backup vào database hoặc cache
            // Có thể tạo bảng movie_backup
            return currentMovie;
        }

        return null;
    }

// Thêm method này vào MovieService.java

    /**
     * Xóa vĩnh viễn phim hoàn toàn khỏi database (đơn giản)
     */
    @Transactional
    public boolean hardDeleteMovieComplete(Integer movieId) {
        if (movieId == null) {
            return false;
        }

        try {
            System.out.println("=== HARD DELETE MOVIE START ===");
            System.out.println("Movie ID: " + movieId);

            // 1. Kiểm tra phim có tồn tại không
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                System.out.println("ERROR: Movie not found");
                return false;
            }

            // 2. Kiểm tra status phải là "Removed"
            if (movie.getStatus() != Movie.MovieStatus.Removed) {
                System.out.println("ERROR: Movie status is not 'Removed': " + movie.getStatus());
                return false;
            }

            String movieName = movie.getMovieName();
            System.out.println("Deleting movie: " + movieName);

            // 3. Xóa các mối quan hệ MovieGenre trước
            movieGenreRepository.deleteByMovieId(movieId);
            System.out.println("Deleted movie-genre relationships");

            // 4. Xóa các mối quan hệ MovieActor trước
            movieActorRepository.deleteByMovieId(movieId);
            System.out.println("Deleted movie-actor relationships");

            // 5. Flush để đảm bảo xóa relationships trước
            movieGenreRepository.flush();
            movieActorRepository.flush();

            // 6. Xóa phim chính
            movieRepository.deleteById(movieId);
            movieRepository.flush();

            // 7. Verify deletion
            boolean stillExists = movieRepository.existsById(movieId);
            if (stillExists) {
                System.out.println("ERROR: Movie still exists after deletion");
                return false;
            }

            System.out.println("SUCCESS: Hard deleted movie '" + movieName + "'");
            System.out.println("=== HARD DELETE MOVIE END ===");
            return true;

        } catch (Exception e) {
            System.err.println("ERROR in hardDeleteMovieComplete: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}