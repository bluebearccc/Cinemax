package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.enumtype.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
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
        return movieRepository.findByStatus(Movie_Status.Active).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim đang chiếu
     */
    public List<MovieDTO> getNowShowingMovies() {
        return movieRepository.findActiveMovies(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim sắp chiếu
     */
    public List<MovieDTO> getUpcomingMovies() {
        return movieRepository.findUpcomingMovies(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim mới nhất
     */
    public List<MovieDTO> getRecentMovies(int limit) {
        List<Movie> movies = movieRepository.findByStatus(Movie_Status.Active);
        return movies.stream()
                .sorted((m1, m2) -> {
                    if (m1.getStartDate() == null && m2.getStartDate() == null) return 0;
                    if (m1.getStartDate() == null) return 1;
                    if (m2.getStartDate() == null) return -1;
                    return m2.getStartDate().compareTo(m1.getStartDate());
                })
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

    //Lấy phim theo genreId (Integer version)

    public List<MovieDTO> getMoviesByGenre(Integer genreId) {
        if (genreId == null) {
            return getAllActiveMovies();
        }

        // SỬ DỤNG JOIN trực tiếp thay vì query phức tạp
        return movieRepository.findMoviesByGenreIdJoin(genreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== SEARCH AND FILTER OPERATIONS ====================


    /**
     * Lấy phim theo diễn viên - FIXED
     */
    public List<MovieDTO> getMoviesByActor(Integer actorId) {
        if (actorId == null) return getAllActiveMovies();

        // SỬ DỤNG @ManyToMany JOIN trực tiếp
        return movieRepository.findMoviesByActorIdJoin(actorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy top phim theo rating - FIXED
     */
    public List<MovieDTO> getTopRatedMovies() {
        return movieRepository.findByStatus(Movie_Status.Active).stream()
                .filter(movie -> movie.getMovieRate() != null)
                .sorted((m1, m2) -> Double.compare(m2.getMovieRate(), m1.getMovieRate()))
                .limit(10)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phim liên quan (cùng thể loại, trừ phim hiện tại) - FIXED
     */
    public List<MovieDTO> getRelatedMovies(Long movieId) {
        if (movieId == null) return List.of();

        // Lấy movie hiện tại
        Movie currentMovie = movieRepository.findById(movieId.intValue()).orElse(null);
        if (currentMovie == null || currentMovie.getGenres().isEmpty()) {
            return List.of();
        }

        // Lấy genre đầu tiên của phim hiện tại
        Integer genreId = currentMovie.getGenres().get(0).getGenreID();

        // Tìm phim cùng thể loại, loại trừ phim hiện tại
        return movieRepository.findMoviesByGenreIdJoin(genreId).stream()
                .filter(movie -> !((Integer) movie.getMovieID()).equals(movieId.intValue()))
                .limit(4)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // ==================== EXISTENCE CHECK OPERATIONS ====================


    /**
     * Kiểm tra tên phim có bị trùng không (trừ phim hiện tại)
     */
    public boolean isMovieNameExists(String movieName, Integer excludeId) {
        if (movieName == null || movieName.trim().isEmpty()) return false;

        List<Movie> movies = movieRepository.findByMovieNameContainingIgnoreCase(movieName.trim());

        // Loại bỏ phim hiện tại khỏi kiểm tra
        if (excludeId != null) {
            movies = movies.stream()
                    .filter(movie -> !((Integer) movie.getMovieID()).equals(excludeId))
                    .collect(Collectors.toList());
        }

        // Kiểm tra tên chính xác
        return movies.stream()
                .anyMatch(movie -> movie.getMovieName().equalsIgnoreCase(movieName.trim()));
    }


    // ==================== UPDATE OPERATIONS - COMPLETELY FIXED ====================

    /**
     * Cập nhật phim hoàn chỉnh bao gồm cả genres - COMPLETELY FIXED VERSION
     */
    @Transactional
    public boolean updateMovieComplete(Integer id, Movie movieDetails, List<Integer> genreIds) {
        if (id == null || movieDetails == null) {
            System.out.println("ERROR: Invalid parameters - id or movieDetails is null");
            return false;
        }

        try {
            System.out.println("=== SERVICE: UPDATE MOVIE COMPLETE START ===");
            System.out.println("Movie ID: " + id);
            System.out.println("Movie details name: " + movieDetails.getMovieName());
            System.out.println("Movie details rating: " + movieDetails.getMovieRate());
            System.out.println("Movie details duration: " + movieDetails.getDuration());

            // 1. Lấy movie hiện tại từ database
            Optional<Movie> optionalMovie = movieRepository.findById(id);
            if (!optionalMovie.isPresent()) {
                System.out.println("ERROR: Movie not found with ID: " + id);
                return false;
            }

            Movie existingMovie = optionalMovie.get();

            System.out.println("=== BEFORE UPDATE ===");
            System.out.println("Current movie name: " + existingMovie.getMovieName());
            System.out.println("Current movie rating: " + existingMovie.getMovieRate());
            System.out.println("Current movie duration: " + existingMovie.getDuration());

            // 2. Cập nhật từng field một cách cẩn thận - ALWAYS UPDATE
            updateMovieFieldsDirectly(existingMovie, movieDetails);

            System.out.println("=== AFTER FIELD UPDATE ===");
            System.out.println("Updated movie name: " + existingMovie.getMovieName());
            System.out.println("Updated movie rating: " + existingMovie.getMovieRate());
            System.out.println("Updated movie duration: " + existingMovie.getDuration());

            // 3. Lưu movie vào database
            Movie savedMovie = movieRepository.save(existingMovie);
            movieRepository.flush(); // Force immediate database sync

            System.out.println("=== AFTER SAVE ===");
            System.out.println("Saved movie ID: " + savedMovie.getMovieID());
            System.out.println("Saved movie name: " + savedMovie.getMovieName());
            System.out.println("Saved movie rating: " + savedMovie.getMovieRate());
            System.out.println("Saved movie duration: " + savedMovie.getDuration());

            // 4. Cập nhật genres
            updateMovieGenresDirectly(savedMovie, genreIds);
            System.out.println("Genres updated successfully");

            // 5. Verify lại từ database
            Movie verifyMovie = movieRepository.findById(id).orElse(null);
            if (verifyMovie != null) {
                System.out.println("=== FINAL VERIFICATION ===");
                System.out.println("Verified movie name: " + verifyMovie.getMovieName());
                System.out.println("Verified movie rating: " + verifyMovie.getMovieRate());
                System.out.println("Verified movie duration: " + verifyMovie.getDuration());

                // Check if update was successful
                boolean nameMatch = movieDetails.getMovieName().equals(verifyMovie.getMovieName());
                boolean durationMatch = Objects.equals(movieDetails.getDuration(), verifyMovie.getDuration());
                boolean ratingMatch = Objects.equals(movieDetails.getMovieRate(), verifyMovie.getMovieRate());

                System.out.println("Name updated correctly: " + nameMatch);
                System.out.println("Duration updated correctly: " + durationMatch);
                System.out.println("Rating updated correctly: " + ratingMatch);

                if (nameMatch && durationMatch && ratingMatch) {
                    System.out.println("✅ ALL UPDATES VERIFIED SUCCESSFULLY");
                    return true;
                } else {
                    System.out.println("❌ SOME UPDATES FAILED VERIFICATION");
                    return false;
                }
            } else {
                System.out.println("ERROR: Cannot verify movie after save");
                return false;
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR in updateMovieComplete: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating movie: " + e.getMessage());
        }
    }

    /**
     * Cập nhật từng field của movie một cách trực tiếp - ALWAYS UPDATE VERSION
     */
    private void updateMovieFieldsDirectly(Movie existingMovie, Movie movieDetails) {
        System.out.println("=== UPDATING MOVIE FIELDS DIRECTLY ===");

        // Update movie name - ALWAYS
        if (movieDetails.getMovieName() != null) {
            existingMovie.setMovieName(movieDetails.getMovieName());
            System.out.println("✓ Updated movie name: " + movieDetails.getMovieName());
        }

        // Update description - ALWAYS (even if null)
        existingMovie.setDescription(movieDetails.getDescription());
        System.out.println("✓ Updated description");

        // Update image - ALWAYS (even if null)
        existingMovie.setImage(movieDetails.getImage());
        System.out.println("✓ Updated image");

        // Update banner - ALWAYS (even if null)
        existingMovie.setBanner(movieDetails.getBanner());
        System.out.println("✓ Updated banner");

        // Update studio - ALWAYS (even if null)
        existingMovie.setStudio(movieDetails.getStudio());
        System.out.println("✓ Updated studio");

        // Update duration - ALWAYS
        if (movieDetails.getDuration() != null) {
            existingMovie.setDuration(movieDetails.getDuration());
            System.out.println("✓ Updated duration: " + movieDetails.getDuration());
        }

        // Update trailer - ALWAYS (even if null)
        existingMovie.setTrailer(movieDetails.getTrailer());
        System.out.println("✓ Updated trailer");

        // Update movie rate - ALWAYS UPDATE (even if null)
        System.out.println("Old rating: " + existingMovie.getMovieRate());
        System.out.println("New rating from form: " + movieDetails.getMovieRate());
        existingMovie.setMovieRate(movieDetails.getMovieRate());
        System.out.println("✓ Updated rating to: " + existingMovie.getMovieRate());

        // Update dates - ALWAYS
        if (movieDetails.getStartDate() != null) {
            existingMovie.setStartDate(movieDetails.getStartDate());
            System.out.println("✓ Updated start date: " + movieDetails.getStartDate());
        }

        if (movieDetails.getEndDate() != null) {
            existingMovie.setEndDate(movieDetails.getEndDate());
            System.out.println("✓ Updated end date: " + movieDetails.getEndDate());
        }

        // Update status - ALWAYS
        if (movieDetails.getStatus() != null) {
            existingMovie.setStatus(movieDetails.getStatus());
            System.out.println("✓ Updated status: " + movieDetails.getStatus());
        }

        System.out.println("=== FIELD UPDATE COMPLETE ===");
    }

    /**
     * Cập nhật genres của phim một cách trực tiếp
     */
    @Transactional
    public void updateMovieGenresDirectly(Movie movie, List<Integer> genreIds) {
        if (movie == null) {
            System.out.println("ERROR: Movie is null in updateMovieGenresDirectly");
            return;
        }

        try {
            System.out.println("=== UPDATING MOVIE GENRES ===");
            System.out.println("Movie ID: " + movie.getMovieID());
            System.out.println("New genre IDs: " + genreIds);

            // 1. Clear existing genres
            if (movie.getGenres() != null) {
                movie.getGenres().clear();
            } else {
                movie.setGenres(new ArrayList<>());
            }

            // 2. Add new genres
            if (genreIds != null && !genreIds.isEmpty()) {
                for (Integer genreId : genreIds) {
                    Genre genre = genreRepository.findById(genreId).orElse(null);
                    if (genre != null) {
                        movie.getGenres().add(genre);
                        System.out.println("✓ Added genre: " + genre.getGenreName());
                    } else {
                        System.out.println("⚠ Genre not found: " + genreId);
                    }
                }
            }

            // 3. Save movie (cascade sẽ tự động update bảng Movie_Genre)
            movieRepository.save(movie);
            System.out.println("=== GENRE UPDATE COMPLETE ===");

        } catch (Exception e) {
            System.err.println("ERROR in updateMovieGenresDirectly: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error updating movie genres: " + e.getMessage());
        }
    }


    // ==================== STATISTICS OPERATIONS ====================

    /**
     * Các phương thức thống kê
     */
    public long countAllMovies() {
        return movieRepository.count();
    }

    public long countNowShowingMovies() {
        return movieRepository.findActiveMovies(LocalDateTime.now()).size();
    }

    public long countUpcomingMovies() {
        return movieRepository.findUpcomingMovies(LocalDateTime.now()).size();
    }

    public double getAverageRating() {
        List<Movie> activeMovies = movieRepository.findByStatus(Movie_Status.Active);
        return activeMovies.stream()
                .filter(movie -> movie.getMovieRate() != null)
                .mapToDouble(Movie::getMovieRate)
                .average()
                .orElse(0.0);
    }


    // ==================== DELETE OPERATIONS ====================



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
            if (movie.getStatus() != Movie_Status.Removed) {
                System.out.println("ERROR: Movie status is not 'Removed': " + movie.getStatus());
                return false;
            }

            String movieName = movie.getMovieName();
            System.out.println("Deleting movie: " + movieName);

            // 3. Xóa phim chính - Hibernate sẽ tự động xóa Movie_Genre và Movie_Actor relationships
            movieRepository.deleteById(movieId);
            movieRepository.flush();

            // 4. Verify deletion
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

    /**
     * Cập nhật ngày hết hạn chiếu của phim - FIXED to use LocalDateTime
     */
    @Transactional
    public boolean updateEndDate(Integer id, LocalDateTime endDate) {
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
            if (movie.getStatus() == Movie_Status.Active && endDate.isBefore(LocalDateTime.now())) {
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
     * Debug method để kiểm tra rating flow
     */
    public void debugMovieRating(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie != null) {
            System.out.println("=== MOVIE RATING DEBUG ===");
            System.out.println("Movie ID: " + movie.getMovieID());
            System.out.println("Movie Name: " + movie.getMovieName());
            System.out.println("Current Rating: " + movie.getMovieRate());
            System.out.println("Rating Class: " + (movie.getMovieRate() != null ? movie.getMovieRate().getClass() : "null"));
            System.out.println("=========================");
        }
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
            if (movie.getMovieRate() < 0.0 || movie.getMovieRate() > 5.0) {
                return "Đánh giá phải từ 0.0 đến 5.0";
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
            movie.setDuration(120);
            System.out.println("Set default duration: 120 minutes");
        }

        // Set default dates nếu null
        if (movie.getStartDate() == null) {
            movie.setStartDate(LocalDateTime.now());
            System.out.println("Set default start date: " + movie.getStartDate());
        }

        if (movie.getEndDate() == null) {
            movie.setEndDate(movie.getStartDate().plusDays(30)); // 30 days showing period
            System.out.println("Set default end date: " + movie.getEndDate());
        }

        // Set default status nếu null
        if (movie.getStatus() == null) {
            movie.setStatus(Movie_Status.Active);
            System.out.println("Set default status: Active");
        }

        // Set default rating nếu null
        if (movie.getMovieRate() == null) {
            movie.setMovieRate(0.0);
            System.out.println("Set default rating: 0.0");
        }

        // Handle string fields với default values cho NOT NULL constraints
        if (movie.getMovieName() != null) {
            movie.setMovieName(movie.getMovieName().trim());
        }

        // Set default cho description nếu null
        if (movie.getDescription() == null || movie.getDescription().trim().isEmpty()) {
            movie.setDescription("Đang cập nhật thông tin phim...");
            System.out.println("Set default description");
        } else {
            movie.setDescription(movie.getDescription().trim());
        }

        // Set default cho studio nếu null
        if (movie.getStudio() == null || movie.getStudio().trim().isEmpty()) {
            movie.setStudio("Chưa xác định");
            System.out.println("Set default studio");
        } else {
            movie.setStudio(movie.getStudio().trim());
        }

        // Set default cho image nếu null
        if (movie.getImage() == null || movie.getImage().trim().isEmpty()) {
            movie.setImage("/images/default-poster.jpg");
            System.out.println("Set default image");
        } else {
            movie.setImage(movie.getImage().trim());
        }

        // Set default cho banner nếu null
        if (movie.getBanner() == null || movie.getBanner().trim().isEmpty()) {
            movie.setBanner("/images/default-banner.jpg");
            System.out.println("Set default banner");
        } else {
            movie.setBanner(movie.getBanner().trim());
        }

        // Set default cho trailer nếu null
        if (movie.getTrailer() == null || movie.getTrailer().trim().isEmpty()) {
            movie.setTrailer("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            System.out.println("Set default trailer");
        } else {
            movie.setTrailer(movie.getTrailer().trim());
        }

        System.out.println("=== DEFAULT VALUES SET ===");
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
            System.out.println("Movie saved with ID: " + savedMovie.getMovieID());

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
            Movie refreshedMovie = movieRepository.findById((Integer) savedMovie.getMovieID()).orElse(null);
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
            // Initialize genres list if null
            if (movie.getGenres() == null) {
                movie.setGenres(new ArrayList<>());
            }

            for (Integer genreId : genreIds) {
                Genre genre = genreRepository.findById(genreId).orElse(null);
                if (genre != null) {
                    movie.getGenres().add(genre);
                } else {
                    System.err.println("Genre not found: " + genreId);
                }
            }

            // Save sẽ tự động update bảng Movie_Genre thông qua cascade
            movieRepository.save(movie);
            System.out.println("Saved " + genreIds.size() + " movie-genre relationships");

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
            // Initialize actors list if null
            if (movie.getActors() == null) {
                movie.setActors(new ArrayList<>());
            }

            for (Integer actorId : actorIds) {
                Actor actor = actorRepository.findById(actorId).orElse(null);
                if (actor != null) {
                    movie.getActors().add(actor);
                } else {
                    System.err.println("Actor not found: " + actorId);
                }
            }

            // Save sẽ tự động update bảng Movie_Actor thông qua cascade
            movieRepository.save(movie);
            System.out.println("Saved " + actorIds.size() + " movie-actor relationships");

        } catch (Exception e) {
            System.err.println("Error adding actors to movie: " + e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi thêm diễn viên cho phim: " + e.getMessage());
        }
    }

    // ==================== CONVERT TO DTO ====================

    /**
     * Convert Entity to DTO - Using Double and LocalDateTime
     */
    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) return null;

        MovieDTO dto = new MovieDTO();
        dto.setMovieId((Integer) movie.getMovieID());
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());

        // Keep movieRate as Double (no conversion needed)
        dto.setMovieRate(movie.getMovieRate());

        // Keep dates as LocalDateTime (no conversion needed)
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());

        dto.setStatus(movie.getStatus() != null ? movie.getStatus().toString() : "Unknown");

        // SIMPLIFIED: Lấy genres trực tiếp từ relationship
        List<String> genreNames = new ArrayList<>();
        if (movie.getGenres() != null) {
            for (Genre genre : movie.getGenres()) {
                genreNames.add(genre.getGenreName());
            }
        }
        dto.setGenres(genreNames);

        // SIMPLIFIED: Lấy actors trực tiếp từ relationship
        List<String> actorNames = new ArrayList<>();
        if (movie.getActors() != null) {
            for (Actor actor : movie.getActors()) {
                actorNames.add(actor.getActorName());
            }
        }
        dto.setActors(actorNames);

        return dto;
    }
}