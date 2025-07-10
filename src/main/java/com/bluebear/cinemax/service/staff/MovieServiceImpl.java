package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.MovieFeedbackDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieFeedback;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.repository.GenreRepository; // Assuming you have a GenreRepository
import com.bluebear.cinemax.repository.MovieRepository; // Assuming this is your Movie entity repository
import com.bluebear.cinemax.repository.ScheduleRepository; // Assuming you have a ScheduleRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieDTO movieDTO = MovieDTO.builder()
                .movieID(movie.getMovieID())
                .movieName(movie.getMovieName())
                .description(movie.getDescription())
                .image(movie.getImage())
                .banner(movie.getBanner())
                .studio(movie.getStudio())
                .duration(movie.getDuration())
                .trailer(movie.getTrailer())
                .movieRate(movie.getMovieRate())
                .startDate(movie.getStartDate())
                .endDate(movie.getEndDate())
                .status(movie.getStatus())
                .build();

        if (movie.getGenres() != null) {
            movieDTO.setGenres(movie.getGenres().stream()
                    .map(this::convertGenreToDTO)
                    .collect(Collectors.toList()));
        }

        if (movie.getFeedbackList() != null) {
            movieDTO.setMovieFeedbacks(movie.getFeedbackList().stream()
                    .map(this::convertMovieFeedbackToDTO)
                    .collect(Collectors.toList()));
        }

        if (movie.getScheduleList() != null) {
            movieDTO.setSchedules(movie.getScheduleList().stream()
                    .map(this::convertScheduleToDTO)
                    .collect(Collectors.toList()));
        }

        return movieDTO;
    }

    private Movie convertToEntity(MovieDTO movieDTO) {
        if (movieDTO == null) {
            return null;
        }

        Movie movie = new Movie();
        movie.setMovieID(movieDTO.getMovieID());
        movie.setMovieName(movieDTO.getMovieName());
        movie.setDescription(movieDTO.getDescription());
        movie.setImage(movieDTO.getImage());
        movie.setBanner(movieDTO.getBanner());
        movie.setStudio(movieDTO.getStudio());
        movie.setDuration(movieDTO.getDuration());
        movie.setTrailer(movieDTO.getTrailer());
        movie.setMovieRate(movieDTO.getMovieRate());
        movie.setStartDate(movieDTO.getStartDate());
        movie.setEndDate(movieDTO.getEndDate());
        movie.setStatus(movieDTO.getStatus());

        if (movieDTO.getGenres() != null && !movieDTO.getGenres().isEmpty()) {
            List<Genre> genres = movieDTO.getGenres().stream()
                    .map(genreDTO -> genreRepository.findById(genreDTO.getGenreID()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            movie.setGenres(genres);
        } else {
            movie.setGenres(null);
        }
        return movie;
    }

    private GenreDTO convertGenreToDTO(Genre genre) {
        if (genre == null) {
            return null;
        }
        return GenreDTO.builder()
                .genreID(genre.getGenreID())
                .genreName(genre.getGenreName())
                .build();
    }

    private MovieFeedbackDTO convertMovieFeedbackToDTO(MovieFeedback feedback) {
        if (feedback == null) {
            return null;
        }
        return MovieFeedbackDTO.builder()
                .id(feedback.getId())
                .customerId(feedback.getCustomer() != null ? feedback.getCustomer().getId() : null)
                .customerName(feedback.getCustomer() != null ? feedback.getCustomer().getFullName() : null)
                .movieId(feedback.getMovie() != null ? feedback.getMovie().getMovieID() : null)
                .content(feedback.getContent())
                .movieRate(feedback.getMovieRate())
                .createdDate(feedback.getCreatedDate())
                .build();
    }

    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        return ScheduleDTO.builder()
                .scheduleID(schedule.getScheduleID())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .movieID(schedule.getMovie() != null ? schedule.getMovie().getMovieID() : null)
                .roomID(schedule.getRoom() != null ? schedule.getRoom().getRoomID() : null)
                .status(schedule.getStatus())
                .build();
    }

    @Override
    public List<MovieDTO> findAllShowingMovies() {
        List<Movie> movies = movieRepository.findAllShowingMovies();
        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieDTO> searchExistingMovieByName(String name) {
        List<Movie> movies = movieRepository.findAllByMovieName(name); // Use containing for partial matches
        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MovieDTO getMovieById(Integer id) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        return movieOptional.map(this::convertToDTO).orElse(null);
    }

    public Page<MovieDTO> findShowingMoviesWithFilters(String name, Integer genreId, String startDateStr, String endDateStr, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        LocalDate startDateVal = (startDateStr != null && !startDateStr.isEmpty()) ? LocalDate.parse(startDateStr) : null;
        LocalDate endDateVal = (endDateStr != null && !endDateStr.isEmpty()) ? LocalDate.parse(endDateStr) : null;

        LocalDateTime startDateTime = (startDateVal != null) ? startDateVal.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDateVal != null) ? endDateVal.atTime(LocalTime.MAX) : null;

        LocalDateTime currentDate = LocalDateTime.now();

        Page<Movie> moviePage = movieRepository.findShowingMoviesWithFilters(
                currentDate, name, genreId, startDateTime, endDateTime, pageable
        );

        return moviePage.map(this::convertToDTO);
    }
}