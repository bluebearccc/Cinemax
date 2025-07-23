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

    @Autowired private MovieRepository movieRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private ActorRepository actorRepository;

    // ==================== READ OPERATIONS ====================

    public List<MovieDTO> getAllMovies() {
        return convertToDTOList(movieRepository.findAll());
    }

    public List<MovieDTO> getAllActiveMovies() {
        return convertToDTOList(movieRepository.findByStatus(Movie_Status.Active));
    }

    public List<MovieDTO> getNowShowingMovies() {
        return convertToDTOList(movieRepository.findActiveMovies(LocalDateTime.now()));
    }

    public List<MovieDTO> getUpcomingMovies() {
        return convertToDTOList(movieRepository.findUpcomingMovies(LocalDateTime.now()));
    }

    public MovieDTO getMovieById(Integer id) {
        return id == null ? null : movieRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    public List<MovieDTO> getMoviesByGenre(Integer genreId) {
        if (genreId == null) return getAllActiveMovies();
        return convertToDTOList(movieRepository.findMoviesByGenreIDJoin(genreId));
    }

    public List<MovieDTO> getMoviesByActor(Integer actorId) {
        if (actorId == null) return getAllActiveMovies();
        return convertToDTOList(movieRepository.findMoviesByActorIdJoin(actorId));
    }

    public List<MovieDTO> getRelatedMovies(Long movieId) {
        if (movieId == null) return List.of();

        Movie currentMovie = movieRepository.findById(movieId.intValue()).orElse(null);
        if (currentMovie == null || currentMovie.getGenres().isEmpty()) return List.of();

        Integer genreId = currentMovie.getGenres().get(0).getGenreID();
        return movieRepository.findMoviesByGenreIDJoin(genreId).stream()
                .filter(movie -> !((Integer) movie.getMovieID()).equals(movieId.intValue()))
                .limit(4)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION ====================

    public boolean isMovieNameExists(String movieName, Integer excludeId) {
        if (movieName == null || movieName.trim().isEmpty()) return false;

        return movieRepository.findByMovieNameContainingIgnoreCase(movieName.trim())
                .stream()
                .filter(movie -> excludeId == null || !((Integer) movie.getMovieID()).equals(excludeId))
                .anyMatch(movie -> movie.getMovieName().equalsIgnoreCase(movieName.trim()));
    }

    private String validateMovieData(Movie movie, boolean isAdd) {
        if (movie == null) return "Dữ liệu phim không hợp lệ";

        // Tên phim bắt buộc
        if (movie.getMovieName() == null || movie.getMovieName().trim().isEmpty())
            return "Tên phim không được để trống";
        if (movie.getMovieName().length() > 255)
            return "Tên phim không được vượt quá 255 ký tự";

        // Mô tả bắt buộc
        if (movie.getDescription() == null || movie.getDescription().trim().isEmpty())
            return "Mô tả phim không được để trống";
        if (movie.getDescription().length() > 2000)
            return "Mô tả không được vượt quá 2000 ký tự";

        // Hãng sản xuất bắt buộc
        if (movie.getStudio() == null || movie.getStudio().trim().isEmpty())
            return "Hãng sản xuất không được để trống";
        if (movie.getStudio().length() > 100)
            return "Tên hãng sản xuất không được vượt quá 100 ký tự";

        // Thời lượng bắt buộc
        if (movie.getDuration() == null)
            return "Thời lượng phim không được để trống";
        if (movie.getDuration() <= 0 || movie.getDuration() > 500)
            return "Thời lượng phim phải từ 1-500 phút";

        // Đánh giá bắt buộc
        if (movie.getMovieRate() == null)
            return "Đánh giá phim không được để trống";
        if (movie.getMovieRate() < 0.0 || movie.getMovieRate() > 5.0)
            return "Đánh giá phải từ 0.0 đến 5.0";

        // Ngày bắt đầu bắt buộc
        if (movie.getStartDate() == null)
            return "Ngày bắt đầu chiếu không được để trống";
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (movie.getStartDate().isBefore(today))
            return "Ngày bắt đầu chiếu phải từ hôm nay trở đi";

        // Ngày kết thúc bắt buộc
        if (movie.getEndDate() == null)
            return "Ngày kết thúc chiếu không được để trống";
        if (movie.getEndDate().isBefore(movie.getStartDate()) || movie.getEndDate().isEqual(movie.getStartDate()))
            return "Ngày kết thúc phải sau ngày bắt đầu";

        // Trạng thái bắt buộc
        if (movie.getStatus() == null)
            return "Trạng thái phim không được để trống";

        if (isAdd && isMovieNameExists(movie.getMovieName(), null))
            return "Tên phim đã tồn tại";

        return null;
    }

    private String validateIds(List<Integer> ids, String type, int maxCount, boolean required) {
        if (required && (ids == null || ids.isEmpty()))
            return String.format("Phải chọn ít nhất một %s", type);
        if (ids == null || ids.isEmpty()) return null;
        if (ids.size() > maxCount) return String.format("Một phim chỉ có thể có tối đa %d %s", maxCount, type);

        for (Integer id : ids) {
            if (id == null) return String.format("%s ID không được null", type);
            boolean exists = type.equals("thể loại") ? genreRepository.existsById(id) : actorRepository.existsById(id);
            if (!exists) return String.format("%s ID %d không tồn tại", type, id);
        }
        return null;
    }

    // ==================== STATUS VALIDATION ====================

    /**
     * Kiểm tra xem movie có thể chuyển từ Active sang Removed hay không
     * @param movieId ID của movie cần kiểm tra
     * @return true nếu có thể chuyển, false nếu không
     */
    private boolean canChangeToRemovedStatus(Integer movieId) {
        if (movieId == null) return false;

        // Kiểm tra xem movie có tồn tại trong schedule không
        return !movieRepository.existsInSchedule(movieId);
    }

    /**
     * Validate việc thay đổi status của movie
     * @param currentMovie Movie hiện tại
     * @param newStatus Status mới
     * @return null nếu hợp lệ, message lỗi nếu không hợp lệ
     */
    private String validateStatusChange(Movie currentMovie, Movie_Status newStatus) {
        if (currentMovie == null || newStatus == null) {
            return "Dữ liệu không hợp lệ";
        }

        Movie_Status currentStatus = currentMovie.getStatus();

        // Nếu status không thay đổi thì OK
        if (currentStatus == newStatus) {
            return null;
        }

        // Kiểm tra chuyển từ Active sang Removed
        if (currentStatus == Movie_Status.Active && newStatus == Movie_Status.Removed) {
            if (!canChangeToRemovedStatus(currentMovie.getMovieID())) {
                return "Không thể xóa phim này vì đang có lịch chiếu";
            }
        }

        // Các trường hợp khác đều cho phép
        return null;
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public boolean updateMovieComplete(Integer movieId, Movie updatedMovie, List<Integer> genreIds, List<Integer> actorIds) {
        try {
            if (movieId == null || updatedMovie == null) {
                return false;
            }

            // Kiểm tra phim có tồn tại không
            Movie existingMovie = movieRepository.findById(movieId).orElse(null);
            if (existingMovie == null) {
                return false;
            }

            // Validate dữ liệu
            String validationError = validateMovieData(updatedMovie, false);
            if (validationError != null) {
                throw new IllegalArgumentException(validationError);
            }

            // Kiểm tra tên phim trùng lặp (ngoại trừ chính nó)
            if (isMovieNameExists(updatedMovie.getMovieName(), movieId)) {
                throw new IllegalArgumentException("Tên phim đã tồn tại");
            }

            // Validate thể loại (bắt buộc)
            String genreValidationError = validateIds(genreIds, "thể loại", 5, true);
            if (genreValidationError != null) {
                throw new IllegalArgumentException(genreValidationError);
            }

            // Validate diễn viên (bắt buộc)
            String actorValidationError = validateIds(actorIds, "diễn viên", 10, true);
            if (actorValidationError != null) {
                throw new IllegalArgumentException(actorValidationError);
            }

            // Validate status change
            String statusValidationError = validateStatusChange(existingMovie, updatedMovie.getStatus());
            if (statusValidationError != null) {
                throw new IllegalArgumentException(statusValidationError);
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
            List<Genre> newGenres = new ArrayList<>();
            for (Integer genreId : genreIds) {
                Genre genre = genreRepository.findById(genreId).orElse(null);
                if (genre != null) {
                    newGenres.add(genre);
                }
            }
            existingMovie.setGenres(newGenres);

            // Cập nhật diễn viên
            List<Actor> newActors = new ArrayList<>();
            for (Integer actorId : actorIds) {
                Actor actor = actorRepository.findById(actorId).orElse(null);
                if (actor != null) {
                    newActors.add(actor);
                }
            }
            existingMovie.setActors(newActors);

            // Lưu vào database
            movieRepository.save(existingMovie);

            // Log để debug
            System.out.println("Movie updated successfully:");
            System.out.println("- ID: " + existingMovie.getMovieID());
            System.out.println("- Image: " + existingMovie.getImage());
            System.out.println("- Banner: " + existingMovie.getBanner());

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật phim hoàn chỉnh: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public boolean updateMovieStatus(Integer movieId, Movie_Status newStatus) {
        try {
            if (movieId == null || newStatus == null) {
                return false;
            }

            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                return false;
            }

            // Kiểm tra điều kiện chuyển đổi status
            String validationError = validateStatusChange(movie, newStatus);
            if (validationError != null) {
                throw new IllegalArgumentException(validationError);
            }

            movie.setStatus(newStatus);
            movieRepository.save(movie);
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật trạng thái phim: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public boolean updateEndDate(Integer movieId, LocalDateTime newEndDate) {
        try {
            if (movieId == null || newEndDate == null) {
                return false;
            }

            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null) {
                return false;
            }

            // Kiểm tra ngày kết thúc không được trước ngày bắt đầu
            if (movie.getStartDate() != null && newEndDate.isBefore(movie.getStartDate())) {
                return false;
            }

            movie.setEndDate(newEndDate);
            movieRepository.save(movie);
            return true;

        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật ngày kết thúc: " + e.getMessage());
            return false;
        }
    }

    // ==================== DELETE OPERATIONS ====================

    @Transactional
    public boolean hardDeleteMovieComplete(Integer movieId) {
        if (movieId == null) return false;

        try {
            Movie movie = movieRepository.findById(movieId).orElse(null);
            if (movie == null || movie.getStatus() != Movie_Status.Removed) return false;

            movieRepository.deleteById(movieId);
            return !movieRepository.existsById(movieId);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== ADD OPERATIONS ====================

    @Transactional
    public MovieDTO addMovie(Movie movie, List<Integer> genreIds, List<Integer> actorIds) {
        if (movie == null) throw new IllegalArgumentException("Dữ liệu phim không được null");

        try {
            String error = validateMovieData(movie, true);
            if (error != null) throw new IllegalArgumentException(error);

            // Validate thể loại (bắt buộc)
            error = validateIds(genreIds, "thể loại", 5, true);
            if (error != null) throw new IllegalArgumentException(error);

            // Validate diễn viên (bắt buộc)
            error = validateIds(actorIds, "diễn viên", 10, true);
            if (error != null) throw new IllegalArgumentException(error);

            setDefaultValues(movie);
            Movie savedMovie = movieRepository.save(movie);
            updateRelationships(savedMovie, genreIds, actorIds);

            return convertToDTO(movieRepository.findById((Integer) savedMovie.getMovieID()).orElse(null));
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi thêm phim: " + e.getMessage());
        }
    }

    // ==================== SCHEDULE CHECK METHODS ====================

    /**
     * Kiểm tra xem movie có schedule không
     * @param movieId ID của movie
     * @return true nếu có schedule, false nếu không
     */
    public boolean hasSchedule(Integer movieId) {
        if (movieId == null) return false;
        return movieRepository.existsInSchedule(movieId);
    }

    /**
     * Kiểm tra xem movie có schedule đang hoạt động không
     * @param movieId ID của movie
     * @return true nếu có schedule active, false nếu không
     */
    public boolean hasActiveSchedule(Integer movieId) {
        if (movieId == null) return false;
        return movieRepository.hasActiveSchedule(movieId);
    }

    /**
     * Lấy số lượng schedule của movie
     * @param movieId ID của movie
     * @return số lượng schedule
     */
    public long getScheduleCount(Integer movieId) {
        if (movieId == null) return 0;
        return movieRepository.countSchedulesByMovieId(movieId);
    }

    // ==================== HELPER METHODS ====================

    private void setDefaultValues(Movie movie) {
        // Không set default cho các field bắt buộc, chỉ set cho field tùy chọn
        movie.setMovieName(movie.getMovieName() != null ? movie.getMovieName().trim() : "");
        movie.setDescription(movie.getDescription() != null ? movie.getDescription().trim() : "");
        movie.setStudio(movie.getStudio() != null ? movie.getStudio().trim() : "");

        // Chỉ set default nếu chưa có giá trị
        if (movie.getImage() == null || movie.getImage().trim().isEmpty()) {
            movie.setImage("/uploads/default-movie.jpg");
        }
        if (movie.getBanner() == null || movie.getBanner().trim().isEmpty()) {
            movie.setBanner("/uploads/default-banner.jpg");
        }
        movie.setTrailer(getOrDefault(movie.getTrailer(), null));
    }

    private String getOrDefault(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    @Transactional
    public void updateRelationships(Movie movie, List<Integer> genreIds, List<Integer> actorIds) {
        // Update genres
        if (genreIds != null) {
            if (movie.getGenres() == null) movie.setGenres(new ArrayList<>());
            else movie.getGenres().clear();

            genreIds.forEach(id -> genreRepository.findById(id).ifPresent(genre -> movie.getGenres().add(genre)));
        }

        // Update actors
        if (actorIds != null) {
            if (movie.getActors() == null) movie.setActors(new ArrayList<>());
            else movie.getActors().clear();

            actorIds.forEach(id -> actorRepository.findById(id).ifPresent(actor -> movie.getActors().add(actor)));
        }

        movieRepository.save(movie);
    }

    // ==================== CONVERSION METHODS ====================

    private List<MovieDTO> convertToDTOList(List<Movie> movies) {
        return movies.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) return null;

        MovieDTO dto = new MovieDTO();
        dto.setMovieId((Integer) movie.getMovieID());
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());

        // SỬ DỤNG TRỰC TIẾP ĐƯỜNG DẪN TỪ DATABASE
        dto.setImage(movie.getImage() != null ? movie.getImage() : "/uploads/default-movie.jpg");
        dto.setBanner(movie.getBanner() != null ? movie.getBanner() : "/uploads/default-banner.jpg");

        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());
        dto.setMovieRate(movie.getMovieRate());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setStatus(movie.getStatus() != null ? movie.getStatus().toString() : "Unknown");

        dto.setGenres(movie.getGenres() != null ?
                movie.getGenres().stream().map(Genre::getGenreName).collect(Collectors.toList()) :
                new ArrayList<>());

        dto.setActors(movie.getActors() != null ?
                movie.getActors().stream().map(Actor::getActorName).collect(Collectors.toList()) :
                new ArrayList<>());

        return dto;
    }

}