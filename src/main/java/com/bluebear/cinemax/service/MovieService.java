package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.enumtype.*;
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

    @Autowired
    private MovieActorRepository movieActorRepository;

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
        return movieRepository.findByStatus(Movie_Status.Active).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả phim đã xóa (Removed)
     */
    public List<MovieDTO> getAllRemovedMovies() {
        return movieRepository.findByStatus(Movie_Status.Removed).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả phim active với phân trang
     */
    public Page<MovieDTO> getAllActiveMoviesWithPaging(PageRequest pageRequest) {
        Page<Movie> moviePage = movieRepository.findAll(pageRequest);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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
     * Lấy phim theo thể loại (Integer version) - FIXED
     */
    public List<MovieDTO> getMoviesByGenre(Integer genreId) {
        if (genreId == null) return getAllActiveMovies();

        // Sử dụng MovieGenreRepository để tìm các movieId theo genreId
        List<Integer> movieIds = movieGenreRepository.findmovieIDsBygenreID(genreId);

        return movieRepository.findAllById(movieIds).stream()
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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
     * Lấy phim theo thể loại và status - FIXED
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
            // Có filter genre - sử dụng MovieGenreRepository
            List<Integer> movieIds = movieGenreRepository.findmovieIDsBygenreID(genreId);
            List<Movie> foundMovies = movieRepository.findAllById(movieIds);

            if ("Active".equals(status)) {
                foundMovies = foundMovies.stream()
                        .filter(movie -> movie.getStatus() == Movie_Status.Active)
                        .collect(Collectors.toList());
            } else if ("Removed".equals(status)) {
                foundMovies = foundMovies.stream()
                        .filter(movie -> movie.getStatus() == Movie_Status.Removed)
                        .collect(Collectors.toList());
            }

            movies = foundMovies.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
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

        List<MovieDTO> movieDTOs = getMoviesByGenre(genreId.intValue());

        // Tính toán phân trang thủ công
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), movieDTOs.size());
        List<MovieDTO> pageContent = movieDTOs.subList(start, end);

        return new PageImpl<>(pageContent, pageRequest, movieDTOs.size());
    }

    /**
     * Lấy phim theo diễn viên - FIXED
     */
    public List<MovieDTO> getMoviesByActor(Integer actorId) {
        if (actorId == null) return getAllActiveMovies();

        // Sử dụng MovieActorRepository để tìm các movieId theo actorId
        List<Integer> movieIds = movieActorRepository.findmovieIDsByactorID(actorId);

        return movieRepository.findAllById(movieIds).stream()
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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

        // Lấy các genreIds của phim hiện tại
        List<Integer> genreIds = movieGenreRepository.findgenreIDsBymovieID(movieId.intValue());
        if (genreIds.isEmpty()) {
            return List.of();
        }

        // Lấy phim cùng thể loại (lấy genre đầu tiên)
        Integer genreId = genreIds.get(0);
        List<Integer> relatedMovieIds = movieGenreRepository.findmovieIDsBygenreID(genreId);

        return movieRepository.findAllById(relatedMovieIds).stream()
                .filter(movie -> !((Integer) movie.getMovieID()).equals(movieId.intValue())) // Loại trừ phim hiện tại
                .filter(movie -> movie.getStatus() == Movie_Status.Active)
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

        return movieRepository.findByStudioContainingIgnoreCaseAndStatus(studio, Movie_Status.Active)
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
                    .filter(movie -> !((Integer) movie.getMovieID()).equals(excludeId))
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
            System.out.println("Setting new rating: " + movieDetails.getMovieRate());
            movie.setMovieRate(movieDetails.getMovieRate());
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
     * Cập nhật genres của phim - IMPROVED VERSION
     */
    @Transactional
    public void updateMovieGenresImproved(Movie movie, List<Integer> genreIds) {
        if (movie == null) return;

        try {
            // 1. Xóa tất cả MovieGenre hiện tại cho phim này
            Integer movieId = (Integer) movie.getMovieID();
            movieGenreRepository.deleteBymovieID(movieId);

            // 2. Flush để đảm bảo delete được thực hiện
            movieGenreRepository.flush();

            // 3. Thêm genres mới
            if (genreIds != null && !genreIds.isEmpty()) {
                for (Integer genreId : genreIds) {
                    Genre genre = genreRepository.findById(genreId).orElse(null);
                    if (genre != null) {
                        MovieGenre movieGenre = new MovieGenre();
                        movieGenre.setMovie(movie);
                        movieGenre.setGenre(genre);
                        movieGenreRepository.save(movieGenre);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating movie genres: " + e.getMessage());
        }
    }

    /**
     * Các phương thức thống kê
     */
    public long countAllMovies() {
        return movieRepository.count();
    }

    public long countActiveMovies() {
        return movieRepository.countByStatus(Movie_Status.Active);
    }

    public long countNowShowingMovies() {
        return movieRepository.findActiveMovies(LocalDate.now()).size();
    }

    public long countUpcomingMovies() {
        return movieRepository.findUpcomingMovies(LocalDate.now()).size();
    }

    public double getAverageRating() {
        List<Movie> activeMovies = movieRepository.findByStatus(Movie_Status.Active);
        return activeMovies.stream()
                .filter(movie -> movie.getMovieRate() != null)
                .mapToDouble(Movie::getMovieRate)
                .average()
                .orElse(0.0);
    }

    /**
     * Lấy phim được thêm gần đây nhất
     */
    public List<MovieDTO> getRecentlyAddedMovies(int limit) {
        // Sử dụng movieID để sort vì ID tăng dần theo thời gian thêm
        return movieRepository.findByStatus(Movie_Status.Active)
                .stream()
                .sorted((m1, m2) -> {
                    Integer id1 = (Integer) m1.getMovieID();
                    Integer id2 = (Integer) m2.getMovieID();
                    if (id1 == null && id2 == null) return 0;
                    if (id1 == null) return 1;
                    if (id2 == null) return -1;
                    return Integer.compare(id2, id1);
                })
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== STATISTICS OPERATIONS ====================

    // ==================== DELETE OPERATIONS ====================

    /**
     * Xóa phim (soft delete - chuyển status thành Removed)
     */
    public boolean deleteMovie(Integer id) {
        if (id == null) return false;

        Optional<Movie> optionalMovie = movieRepository.findById(id);
        if (optionalMovie.isPresent()) {
            Movie movie = optionalMovie.get();
            movie.setStatus(Movie_Status.Removed);
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

            // 3. Xóa các mối quan hệ MovieGenre trước
            movieGenreRepository.deleteBymovieID(movieId);
            System.out.println("Deleted movie-genre relationships");

            // 4. Xóa các mối quan hệ MovieActor trước
            movieActorRepository.deleteBymovieID(movieId);
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

    /**
     * Convert Entity to DTO - FIXED VERSION
     */
    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) return null;

        MovieDTO dto = new MovieDTO();
        dto.setMovieId((Integer) movie.getMovieID()); // Cast Object to Integer
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());

        // Handle Double to BigDecimal conversion for movieRate
        if (movie.getMovieRate() != null) {
            dto.setMovieRate(BigDecimal.valueOf(movie.getMovieRate()).setScale(1, RoundingMode.HALF_UP));
        } else {
            dto.setMovieRate(null);
        }

        // Handle LocalDateTime to LocalDate conversion - FIXED
        if (movie.getStartDate() != null) {
            dto.setStartDate(movie.getStartDate().toLocalDate());
        }

        if (movie.getEndDate() != null) {
            dto.setEndDate(movie.getEndDate().toLocalDate());
        }

        dto.setStatus(movie.getStatus() != null ? movie.getStatus().toString() : "Unknown");

        // Lấy danh sách thể loại từ MovieGenre relationship - FIXED
        Integer movieId = (Integer) movie.getMovieID();
        List<Integer> genreIds = movieGenreRepository.findgenreIDsBymovieID(movieId);
        List<String> genreNames = new ArrayList<>();
        for (Integer genreId : genreIds) {
            Optional<Genre> genre = genreRepository.findById(genreId);
            if (genre.isPresent()) {
                genreNames.add(genre.get().getGenreName());
            }
        }
        dto.setGenres(genreNames);

        // Lấy danh sách diễn viên từ MovieActor relationship - FIXED
        List<Integer> actorIds = movieActorRepository.findactorIDsBymovieID(movieId);
        List<String> actorNames = new ArrayList<>();
        for (Integer actorId : actorIds) {
            Optional<Actor> actor = actorRepository.findById(actorId);
            if (actor.isPresent()) {
                actorNames.add(actor.get().getActorName());
            }
        }
        dto.setActors(actorNames);

        return dto;
    }

    // ==================== UTILITY OPERATIONS - FIXED ====================

    /**
     * Cập nhật ngày hết hạn chiếu của phim - FIXED to use LocalDate
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

            // Convert LocalDate to LocalDateTime for comparison
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // Validate business logic - endDate phải sau startDate
            if (movie.getStartDate() != null && endDateTime.isBefore(movie.getStartDate())) {
                System.err.println("UpdateEndDate: End date cannot be before start date");
                throw new IllegalArgumentException("Ngày kết thúc không thể trước ngày bắt đầu chiếu");
            }

            // Validate endDate không được ở quá khứ (nếu phim đang active)
            if (movie.getStatus() == Movie_Status.Active && endDateTime.isBefore(LocalDateTime.now())) {
                System.err.println("UpdateEndDate: End date cannot be in the past for active movies");
                throw new IllegalArgumentException("Ngày kết thúc không thể ở quá khứ đối với phim đang chiếu");
            }

            // Log thông tin cập nhật
            System.out.println("UpdateEndDate: Updating movie '" + movie.getMovieName() + "' (ID: " + id + ")");
            System.out.println("Old end date: " + movie.getEndDate());
            System.out.println("New end date: " + endDateTime);

            // Cập nhật ngày kết thúc
            movie.setEndDate(endDateTime);

            // Lưu vào database
            Movie savedMovie = movieRepository.save(movie);

            // Verify save operation
            if (savedMovie != null && endDateTime.equals(savedMovie.getEndDate())) {
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
            for (Integer genreId : genreIds) {
                Genre genre = genreRepository.findById(genreId).orElse(null);
                if (genre != null) {
                    MovieGenre movieGenre = new MovieGenre();
                    movieGenre.setMovie(movie);
                    movieGenre.setGenre(genre);
                    movieGenreRepository.save(movieGenre);
                } else {
                    System.err.println("Genre not found: " + genreId);
                }
            }
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
            for (Integer actorId : actorIds) {
                Actor actor = actorRepository.findById(actorId).orElse(null);
                if (actor != null) {
                    MovieActor movieActor = new MovieActor();
                    movieActor.setMovie(movie);
                    movieActor.setActor(actor);
                    movieActorRepository.save(movieActor);
                } else {
                    System.err.println("Actor not found: " + actorId);
                }
            }
            System.out.println("Saved " + actorIds.size() + " movie-actor relationships");

        } catch (Exception e) {
            System.err.println("Error adding actors to movie: " + e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra khi thêm diễn viên cho phim: " + e.getMessage());
        }
    }
}