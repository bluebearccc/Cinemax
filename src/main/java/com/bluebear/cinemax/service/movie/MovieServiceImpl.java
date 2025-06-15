package com.bluebear.cinemax.service.movie;

import com.bluebear.cinemax.constant.Constant;
import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.repository.GenreRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    private GenreService genreService;
    @Autowired
    private MovieFeedbackService movieFeedbackService;
    @Autowired
    private ScheduleService scheduleService;

    private Movie convertToEntity(MovieDTO dto) {
        List<Genre> genres = dto.getGenres() != null ? dto.getGenres().stream().map(genreDTO -> genreService.convertToEntity(genreDTO)).collect(Collectors.toList()) : List.of();

        Movie movie = new Movie();
        movie.setMovieID(dto.getMovieID());
        movie.setMovieName(dto.getMovieName());
        movie.setDescription(dto.getDescription());
        movie.setImage(dto.getImage());
        movie.setBanner(dto.getBanner());
        movie.setStudio(dto.getStudio());
        movie.setDuration(dto.getDuration());
        movie.setTrailer(dto.getTrailer());
        movie.setMovieRate(dto.getMovieRate());
        movie.setActor(dto.getActor());
        movie.setStartDate(dto.getStartDate());
        movie.setEndDate(dto.getEndDate());
        movie.setStatus(dto.getStatus());
        movie.setGenres(genres);
        if (dto.getMovieFeedbacks() != null) {movie.setFeedbackList(dto.getMovieFeedbacks().stream().map(movieFeedbackDTO -> movieFeedbackService.fromDTO(movieFeedbackDTO)).collect(Collectors.toList()));}
        if (dto.getSchedules() != null) {movie.setScheduleList(dto.getSchedules().stream().map(scheduleDTO -> scheduleService.toEntity(scheduleDTO)).collect(Collectors.toList()));}
        return movie;
    }

    private MovieDTO convertToDTO(Movie movie) {
        List<GenreDTO> genreDTOs = movie.getGenres().stream()
                .map(g -> {
                    GenreDTO dto = new GenreDTO();
                    dto.setGenreID(g.getGenreID());
                    dto.setGenreName(g.getGenreName());
                    return dto;
                })
                .collect(Collectors.toList());

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
        dto.setActor(movie.getActor());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setStatus(movie.getStatus());
        dto.setGenres(genreDTOs);
        return dto;
    }


    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = convertToEntity(movieDTO);
        Movie saved = movieRepository.save(movie);
        return convertToDTO(saved);
    }

    public List<MovieDTO> findAllByStatus(Pageable pageable) {
        return movieRepository.findAllByStatus(Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MovieDTO> getMovieById(Integer id) {
        return movieRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public Optional<MovieDTO> updateMovie(Integer id, MovieDTO movieDTO) {
        return movieRepository.findById(id).map(existing -> {
            Movie movie = convertToEntity(movieDTO);
            movie.setMovieID(id);
            Movie updated = movieRepository.save(movie);
            return convertToDTO(updated);
        });
    }

    @Transactional
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }

    @Transactional
    public List<MovieDTO> findMoviesByGenre(Integer genreId, Pageable pageable) {
        return movieRepository.findByGenreIdAndStatus(genreId, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MovieDTO findMovieByHighestRate() {
        return convertToDTO(movieRepository.findTop1ByStatusOrderByMovieRateDesc(Movie_Status.Active));
    }

    @Transactional
    public List<MovieDTO> findTop3MoviesHighestRate() {
        return movieRepository.findTop3ByStatusOrderByMovieRateDesc(Movie_Status.Active).stream().map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByName(String movieName, Pageable pageable) {
        return movieRepository.findBymovieNameContainingIgnoreCaseAndStatus(movieName, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByGenreAndName(Integer genreId, String movieName, Pageable pageable) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return movieRepository.findByGenresAndMovieNameContainingIgnoreCaseAndStatus(genre, movieName, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByGenreAndNameOrderByRateDesc(Integer genreId, String movieName, Pageable pageable) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return movieRepository.findMoviesByGenresAndMovieNameContainingIgnoreCaseAndStatusOrderByMovieRateDesc(genre, movieName, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByNameOrderByRateDesc(String movieName, Pageable pageable) {
        return movieRepository.findMovesByMovieNameContainingIgnoreCaseAndStatusOrderByMovieRateDesc(movieName, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByGenreOrderByRateDesc(Integer genreId, Pageable pageable) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return movieRepository.findByGenresAndStatusOrderByMovieRateDesc(genre, Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findAllByStatusOrderByMovieRateDesc(Pageable pageable) {
        return movieRepository.findAllByStatusOrderByMovieRateDesc(Movie_Status.Active, pageable).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findAllMoviesCurrentlyShow() {
        return movieRepository.findMoviesByStartDateBeforeAndEndDateAfterOrderByMovieRateDesc(new Date(), new Date()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByScheduleToday(Date today) {
        return movieRepository.findMoviesWithScheduleToday(today).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MovieDTO> findMoviesByScheduleAndTheater(Date schedule, int theaterId) {
        List<MovieDTO> movieDTOS = movieRepository.findMoviesWithScheduleTodayWithTheater(theaterId, schedule).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        for (MovieDTO movieDTO : movieDTOS) {
            List<ScheduleDTO> scheduleDTOS = scheduleService.getScheduleByMovieIdAndDate(movieDTO.getMovieID(), schedule);
            movieDTO.setSchedules(scheduleDTOS);
        }

        return movieDTOS;
    }

    @Transactional
    public List<MovieDTO> findAllMoviesWillShow() {
        return movieRepository.findMoviesByStartDateAfter(new Date()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public int countNumberOfPage() {
        int totalMovies = (int) movieRepository.countMovieByStatus(Movie_Status.Active);
        return (int) Math.ceil((double) totalMovies / Constant.MOVIES_PER_PAGE);
    }

    @Transactional
    public int countNumberOfPage(List<MovieDTO> movies) {
        return (int) Math.ceil((double) movies.size() / Constant.MOVIES_PER_PAGE);
    }

    @Transactional
    public int countNumberOfPageByName(String movieName) {
        return (int) Math.ceil((double) movieRepository.countMovieByMovieNameContainingAndStatus(movieName, Movie_Status.Active) / Constant.MOVIES_PER_PAGE);
    }

    @Transactional
    public int countNumberOfPageByGenreId(Integer genreId) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return (int) Math.ceil((double) movieRepository.countMovieByGenresAndStatus(genre, Movie_Status.Active) / Constant.MOVIES_PER_PAGE);
    }

    @Transactional
    public int countNumberOfPageByGenreAndByName(Integer genreId, String movieName) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return (int) Math.ceil((double) movieRepository.countMovieByGenresAndMovieNameContainingAndStatus(genre, movieName, Movie_Status.Active) / Constant.MOVIES_PER_PAGE);
    }
}
