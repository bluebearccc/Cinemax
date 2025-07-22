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
        if (movie.getMovieName() == null || movie.getMovieName().trim().isEmpty())
            return "Tên phim không được để trống";
        if (movie.getMovieName().length() > 100)
            return "Tên phim không được vượt quá 100 ký tự";

        if (isAdd && isMovieNameExists(movie.getMovieName(), null))
            return "Tên phim đã tồn tại";

        if (movie.getDuration() != null && movie.getDuration() <= 0)
            return "Thời lượng phim phải lớn hơn 0";
        if (movie.getMovieRate() != null && (movie.getMovieRate() < 0.0 || movie.getMovieRate() > 5.0))
            return "Đánh giá phải từ 0.0 đến 5.0";
        if (movie.getStartDate() != null && movie.getEndDate() != null &&
                movie.getStartDate().isAfter(movie.getEndDate()))
            return "Ngày bắt đầu chiếu phải trước ngày kết thúc";

        return null;
    }

    private String validateIds(List<Integer> ids, String type, int maxCount) {
        if (ids == null || ids.isEmpty()) return null;
        if (ids.size() > maxCount) return String.format("Một phim chỉ có thể có tối đa %d %s", maxCount, type);

        for (Integer id : ids) {
            if (id == null) return String.format("%s ID không được null", type);
            boolean exists = type.equals("thể loại") ? genreRepository.existsById(id) : actorRepository.existsById(id);
            if (!exists) return String.format("%s ID %d không tồn tại", type, id);
        }
        return null;
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public boolean updateMovieComplete(Integer id, Movie movieDetails, List<Integer> genreIds) {
        if (id == null || movieDetails == null) return false;

        try {
            Movie existingMovie = movieRepository.findById(id).orElse(null);
            if (existingMovie == null) return false;

            // Copy fields
            existingMovie.setMovieName(movieDetails.getMovieName());
            existingMovie.setDescription(movieDetails.getDescription());
            existingMovie.setImage(movieDetails.getImage());
            existingMovie.setBanner(movieDetails.getBanner());
            existingMovie.setStudio(movieDetails.getStudio());
            existingMovie.setDuration(movieDetails.getDuration());
            existingMovie.setTrailer(movieDetails.getTrailer());
            existingMovie.setMovieRate(movieDetails.getMovieRate());
            existingMovie.setStartDate(movieDetails.getStartDate());
            existingMovie.setEndDate(movieDetails.getEndDate());
            existingMovie.setStatus(movieDetails.getStatus());

            Movie savedMovie = movieRepository.save(existingMovie);
            updateRelationships(savedMovie, genreIds, null);

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error updating movie: " + e.getMessage());
        }
    }

    @Transactional
    public boolean updateEndDate(Integer id, LocalDateTime endDate) {
        if (id == null || endDate == null) return false;

        try {
            Movie movie = movieRepository.findById(id).orElse(null);
            if (movie == null) return false;

            if (movie.getStartDate() != null && endDate.isBefore(movie.getStartDate()))
                throw new IllegalArgumentException("Ngày kết thúc không thể trước ngày bắt đầu chiếu");
            if (movie.getStatus() == Movie_Status.Active && endDate.isBefore(LocalDateTime.now()))
                throw new IllegalArgumentException("Ngày kết thúc không thể ở quá khứ đối với phim đang chiếu");

            movie.setEndDate(endDate);
            movieRepository.save(movie);
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi cập nhật ngày hết hạn chiếu: " + e.getMessage());
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

            error = validateIds(genreIds, "thể loại", 5);
            if (error != null) throw new IllegalArgumentException(error);

            error = validateIds(actorIds, "diễn viên", 20);
            if (error != null) throw new IllegalArgumentException(error);

            setDefaultValues(movie);
            Movie savedMovie = movieRepository.save(movie);
            updateRelationships(savedMovie, genreIds, actorIds);

            return convertToDTO(movieRepository.findById((Integer) savedMovie.getMovieID()).orElse(null));
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi thêm phim: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private void setDefaultValues(Movie movie) {
        if (movie.getDuration() == null) movie.setDuration(120);
        if (movie.getStartDate() == null) movie.setStartDate(LocalDateTime.now());
        if (movie.getEndDate() == null) movie.setEndDate(movie.getStartDate().plusDays(30));
        if (movie.getStatus() == null) movie.setStatus(Movie_Status.Active);
        if (movie.getMovieRate() == null) movie.setMovieRate(0.0);

        movie.setMovieName(movie.getMovieName() != null ? movie.getMovieName().trim() : "");
        movie.setDescription(getOrDefault(movie.getDescription(), "Đang cập nhật thông tin phim..."));
        movie.setStudio(getOrDefault(movie.getStudio(), "Chưa xác định"));
        movie.setImage(getOrDefault(movie.getImage(), "/images/default-poster.jpg"));
        movie.setBanner(getOrDefault(movie.getBanner(), "/images/default-banner.jpg"));
        movie.setTrailer(getOrDefault(movie.getTrailer(), "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
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
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
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