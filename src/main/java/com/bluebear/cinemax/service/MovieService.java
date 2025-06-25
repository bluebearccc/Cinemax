package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public Page<MovieDTO> findMoviesByTheaterAndDateRange(Integer theaterId, Movie_Status status,
                                                          Theater_Status theaterStatus,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndDateRange(
                theaterId, status, theaterStatus, startDate, endDate, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    public Page<MovieDTO> findMoviesByTheaterAndGenreAndDateRange(Integer theaterId, Integer genreId,
                                                                  Movie_Status status, Theater_Status theaterStatus,
                                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                                  Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndGenreIdAndDateRange(
                theaterId, genreId, status, theaterStatus, startDate, endDate, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    public Page<MovieDTO> findMoviesByTheaterAndKeywordAndDateRange(Integer theaterId, String keyword,
                                                                    Movie_Status status, Theater_Status theaterStatus,
                                                                    LocalDateTime startDate, LocalDateTime endDate,
                                                                    Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndKeywordAndDateRange(
                theaterId, keyword, status, theaterStatus, startDate, endDate, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    public Page<MovieDTO> findMoviesByTheaterAndGenreAndKeywordAndDateRange(Integer theaterId, Integer genreId,
                                                                            String keyword, Movie_Status status,
                                                                            Theater_Status theaterStatus,
                                                                            LocalDateTime startDate, LocalDateTime endDate,
                                                                            Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndGenreIdAndKeywordAndDateRange(
                theaterId, genreId, keyword, status, theaterStatus, startDate, endDate, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    private MovieDTO convertToDTO(Movie movie) {
        if (movie == null) {
            return null;
        }

        MovieDTO dto = new MovieDTO();
        dto.setMovieID(movie.getMovieID());
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
        dto.setStatus(movie.getStatus());

         if (movie.getGenres() != null) {
             List<GenreDTO> genreDTOs = movie.getGenres().stream()
                 .map(this::convertGenreToDTO)
                 .collect(Collectors.toList());
             dto.setGenres(genreDTOs);
         }

        // Convert schedules if needed
         if (movie.getScheduleList() != null) {
             List<ScheduleDTO> scheduleDTOs = movie.getScheduleList().stream()
                 .map(this::convertScheduleToDTO)
                 .collect(Collectors.toList());
             dto.setSchedules(scheduleDTOs);
         }

        return dto;
    }


    private GenreDTO convertGenreToDTO(Genre genre) {
        if (genre == null) return null;
        GenreDTO dto = new GenreDTO();
        dto.setGenreID(genre.getGenreID());
        dto.setGenreName(genre.getGenreName());
        return dto;
    }

    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        if (schedule == null) return null;
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleID());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        // Add other fields as needed
        return dto;
    }

    public MovieDTO findById(Integer movieID) {
        return movieRepository.findById(movieID)
                .map(this::convertToDTO)
                .orElse(null);
    }
}