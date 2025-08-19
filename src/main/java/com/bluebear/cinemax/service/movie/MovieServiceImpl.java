package com.bluebear.cinemax.service.movie;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.AgeLimit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.GenreRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.service.actor.ActorServiceImpl;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
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
    @Autowired
    private ActorServiceImpl actorService;
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private TheaterRepository theaterRepository;

    public Movie toEntity(MovieDTO dto) {
        List<Genre> genres = dto.getGenres() != null
                ? dto.getGenres().stream()
                .map(genreDTO -> genreService.toEntity(genreDTO))
                .collect(Collectors.toList())
                : List.of();

        Movie movie = new Movie();
        movie.setMovieID(dto.getMovieID());
        movie.setMovieName(dto.getMovieName());
        movie.setAgeLimit(dto.getAgeLimit());
        movie.setDescription(dto.getDescription());
        movie.setImage(dto.getImage());
        movie.setBanner(dto.getBanner());
        movie.setStudio(dto.getStudio());
        movie.setDuration(dto.getDuration());
        movie.setTrailer(dto.getTrailer());
        movie.setMovieRate(dto.getMovieRate());
        movie.setStartDate(dto.getStartDate());
        movie.setEndDate(dto.getEndDate());
        movie.setStatus(dto.getStatus());
        movie.setGenres(genres);
        return movie;
    }

    public MovieDTO toDTO(Movie movie) {
        List<GenreDTO> genreDTOs = movie.getGenres() != null
                ? movie.getGenres().stream()
                .map(g -> {
                    GenreDTO dto = new GenreDTO();
                    dto.setGenreID(g.getGenreID());
                    dto.setGenreName(g.getGenreName());
                    return dto;
                })
                .collect(Collectors.toList())
                : List.of();

        MovieDTO dto = new MovieDTO();
        dto.setMovieID(movie.getMovieID());
        dto.setMovieName(movie.getMovieName());
        dto.setAgeLimit(movie.getAgeLimit());
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
        dto.setGenres(genreDTOs);
        return dto;
    }

    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = toEntity(movieDTO);
        Movie saved = movieRepository.save(movie);
        return toDTO(saved);
    }

    public Page<MovieDTO> findAllByStatus(Pageable pageable) {
        return movieRepository.findAllByStatus(Movie_Status.Active, pageable)
                .map(this::toDTO);
    }

    public Optional<MovieDTO> getMovieById(Integer id) {
        return movieRepository.findById(id)
                .map(this::toDTO);
    }

    public Optional<MovieDTO> updateMovie(Integer id, MovieDTO movieDTO) {
        return movieRepository.findById(id).map(existing -> {
            Movie movie = toEntity(movieDTO);
            movie.setMovieID(id);
            Movie updated = movieRepository.save(movie);
            return toDTO(updated);
        });
    }

    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }

    public Page<MovieDTO> findMoviesByGenre(Integer genreId, Pageable pageable) {
        return movieRepository.findByGenreIdAndStatus(genreId, Movie_Status.Active, pageable)
                .map(this::toDTO);
    }

    public MovieDTO findMovieByHighestRate() {
        return toDTO(movieRepository.findTop1ByStatusOrderByMovieRateDesc(Movie_Status.Active));
    }

    public Page<MovieDTO> findTop3MoviesHighestRate() {
        return movieRepository.findTopCurrentlyShowByStatusOrderByMovieRateDesc(Movie_Status.Active, LocalDateTime.now(), PageRequest.of(0, 3)).map(this::toDTO);
    }

    public Page<MovieDTO> findTop5MoviesHighestRate() {
        return movieRepository.findTopCurrentlyShowByStatusOrderByMovieRateDesc(Movie_Status.Active, LocalDateTime.now(), PageRequest.of(0, 5)).map(this::toDTO);
    }

    public Page<MovieDTO> findMoviesByName(String movieName, Pageable pageable) {
        return movieRepository.findBymovieNameContainingIgnoreCaseAndStatus(movieName, Movie_Status.Active, pageable).map(this::toDTO);
    }

    public Page<MovieDTO> findMoviesByGenreAndName(Integer genreId, String movieName, Pageable pageable) {
        Genre genre = genreRepository.findById(genreId).orElseThrow();
        return movieRepository.findByGenresAndMovieNameContainingIgnoreCaseAndStatus(genre, movieName, Movie_Status.Active, pageable)
                .map(this::toDTO);
    }

    public Page<MovieDTO> findAllByStatusOrderByMovieRateDesc(Pageable pageable) {
        Sort sort = Sort.by("movieRate").descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return movieRepository.findAllByStatus(Movie_Status.Active, pageable)
                .map(this::toDTO);
    }

    public Page<MovieDTO> findAllMoviesCurrentlyShow() {
        Sort sort = Sort.by("movieRate").descending();
        Pageable pageable = Pageable.unpaged(sort);
        return movieRepository.findMoviesByStartDateBeforeAndEndDateAfter(LocalDateTime.now(), LocalDateTime.now(), pageable)
                .map(this::toDTO);
    }

    public MovieDTO findMovieByIdWithGenresAndActors(Integer id) {
        MovieDTO movieDTO = getMovieById(id).orElse(null);
        if (movieDTO != null) {
            List<ActorDTO> actors = actorService.getActorsByMovieId(id).getContent();
            movieDTO.setActors(actors);
        }

        return movieDTO;
    }

    public Page<MovieDTO> findMoviesByScheduleToday(LocalDateTime today) {
        return movieRepository.findMoviesWithScheduleToday(today, Pageable.unpaged())
                .map(this::toDTO);
    }

    public Page<MovieDTO> findMoviesByScheduleAndTheaterAndRoomType(LocalDateTime schedule, int theaterId, String roomType) {
        Page<MovieDTO> movieDTOS = movieRepository.findMoviesWithScheduleTodayWithTheaterAndRoomType(theaterId, schedule, roomType, Pageable.unpaged()).map(this::toDTO);
        Iterator<MovieDTO> iterator = movieDTOS.iterator();
        while (iterator.hasNext()) {
            MovieDTO movieDTO = iterator.next();
            List<ScheduleDTO> scheduleDTOS = scheduleService.getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(movieDTO.getMovieID(), theaterId, schedule, roomType).getContent();
            for (ScheduleDTO scheduleDTO : scheduleDTOS) {
                scheduleService.calculateNumOfSeatLeft(scheduleDTO);
            }
            if (scheduleDTOS.isEmpty()) {
                iterator.remove();
            } else {
                movieDTO.setSchedules(scheduleDTOS);
            }
        }
        return movieDTOS;
    }

    public Page<MovieDTO> findAllMoviesWillShow() {
        return movieRepository.findMoviesByStartDateAfter(LocalDateTime.now(), Pageable.unpaged())
                .map(this::toDTO);
    }

    public Page<MovieDTO> findMovies(Integer theaterId, Integer genreId, String movieName, Pageable pageable) {
        Movie_Status status = Movie_Status.Active;
        Page<MovieDTO> movieDTOS = null;
        if (theaterId != null && genreId != null) {
            movieDTOS = movieRepository.findMoviesByTheaterIdAndGenreIdAndMovieNameAndStatus(theaterId, genreId, movieName, status, LocalDateTime.now(), pageable).map(this::toDTO);
        } else if (theaterId != null) {
            movieDTOS = movieRepository.findMoviesByTheaterIdAndMovieNameAndStatus(theaterId, movieName, status, LocalDateTime.now(), pageable).map(this::toDTO);
        } else if (genreId != null) {
            movieDTOS = movieRepository.findMoviesByGenreIdAndMovieNameAndStatus(genreId, movieName, status, pageable).map(this::toDTO);
        } else {
            movieDTOS = movieRepository.findMoviesByMovieNameAndStatus(movieName, status, pageable).map(this::toDTO);
        }

        for (MovieDTO movieDTO : movieDTOS) {
            movieDTO.setTicketSold((int) detailSeatRepository.countTotalTicketsSold(movieDTO.getMovieID()));
        }

        return movieDTOS;
    }

    public Page<MovieDTO> findMoviesThatHaveFeedback(Pageable pageable) {
        return movieRepository.findMoviesThatHaveFeedback(Movie_Status.Active, pageable).map(this::toDTO);
    }

    // ==================== [PHẦN ĐƯỢC SỬA] - Bắt đầu ====================
    // Cập nhật các phương thức để chấp nhận List<AgeLimit>
    @Override
    public Page<MovieDTO> findMoviesByTheaterAndDateRange(Integer theaterId, Movie_Status status,
                                                          Theater_Status theaterStatus,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          List<AgeLimit> ageLimits, Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndDateRange(
                theaterId, status, theaterStatus, startDate, endDate, ageLimits, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    @Override
    public MovieDTO findById(Integer movieID) {
        Optional<Movie> found = movieRepository.findById(movieID);
        return found.map(this::toDTO).orElse(null);
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

        return moviePage.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> findMoviesByTheaterAndGenreAndDateRange(Integer theaterId, Integer genreId,
                                                                  Movie_Status status, Theater_Status theaterStatus,
                                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                                  List<AgeLimit> ageLimits, Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndGenreIdAndDateRange(
                theaterId, genreId, status, theaterStatus, startDate, endDate, ageLimits, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    @Override
    public Page<MovieDTO> findMoviesByTheaterAndKeywordAndDateRange(Integer theaterId, String keyword,
                                                                    Movie_Status status, Theater_Status theaterStatus,
                                                                    LocalDateTime startDate, LocalDateTime endDate,
                                                                    List<AgeLimit> ageLimits, Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndKeywordAndDateRange(
                theaterId, keyword, status, theaterStatus, startDate, endDate, ageLimits, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }

    @Override
    public Page<MovieDTO> findMoviesByTheaterAndGenreAndKeywordAndDateRange(Integer theaterId, Integer genreId,
                                                                            String keyword, Movie_Status status,
                                                                            Theater_Status theaterStatus,
                                                                            LocalDateTime startDate, LocalDateTime endDate,
                                                                            List<AgeLimit> ageLimits, Pageable pageable) {
        Page<Movie> moviesPage = movieRepository.findByTheaterIdAndGenreIdAndKeywordAndDateRange(
                theaterId, genreId, keyword, status, theaterStatus, startDate, endDate, ageLimits, pageable);

        List<MovieDTO> movieDTOs = moviesPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviesPage.getTotalElements());
    }
    // ==================== [PHẦN ĐƯỢC SỬA] - Kết thúc ====================

    @Override
    public List<MovieDTO> findAllShowingMovies() {
        List<Movie> movies = movieRepository.findAllShowingMovies();
        return movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieDTO> searchExistingMovieByName(String name) {
        List<Movie> movies = movieRepository.findAllByMovieName(name);
        return movies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- CÁC HÀM MỚI DÀNH RIÊNG CHO CASHIER ---
    @Override
    public List<LocalDate> getAvailableScheduleDatesForCashier(Integer theaterId, LocalDateTime startDate, LocalDateTime endDate) {
        return movieRepository.findDistinctScheduleDatesForCashier(theaterId, startDate, endDate);
    }

    @Override
    public Page<MovieDTO> findMoviesForCashierByAllFilters(Integer theaterId, Integer genreId, String keyword, Movie_Status status, Theater_Status theaterStatus, LocalDateTime startDate, LocalDateTime endDate, List<AgeLimit> ageLimits, Pageable pageable) {
        return movieRepository.findForCashierByAllFilters(theaterId, genreId, keyword, status, theaterStatus, startDate, endDate, ageLimits, pageable).map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> findMoviesForCashierByKeyword(Integer theaterId, String keyword, Movie_Status status, Theater_Status theaterStatus, LocalDateTime startDate, LocalDateTime endDate, List<AgeLimit> ageLimits, Pageable pageable) {
        return movieRepository.findForCashierByKeyword(theaterId, keyword, status, theaterStatus, startDate, endDate, ageLimits, pageable).map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> findMoviesForCashierByGenre(Integer theaterId, Integer genreId, Movie_Status status, Theater_Status theaterStatus, LocalDateTime startDate, LocalDateTime endDate, List<AgeLimit> ageLimits, Pageable pageable) {
        return movieRepository.findForCashierByGenre(theaterId, genreId, status, theaterStatus, startDate, endDate, ageLimits, pageable).map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> findMoviesForCashier(Integer theaterId, Movie_Status status, Theater_Status theaterStatus, LocalDateTime startDate, LocalDateTime endDate, List<AgeLimit> ageLimits, Pageable pageable) {
        return movieRepository.findForCashierByTheater(theaterId, status, theaterStatus, startDate, endDate, ageLimits, pageable).map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> findMoviesByScheduleAndTheaterAndRoomTypeAndGenre(LocalDateTime schedule, int theaterId, String roomType, String genreName) {
        Page<MovieDTO> movieDTOS = movieRepository.findMoviesWithScheduleTodayWithTheaterAndRoomTypeAndGenre(theaterId, schedule, roomType, Pageable.unpaged(), Movie_Status.Active, genreName).map(this::toDTO);
        Iterator<MovieDTO> iterator = movieDTOS.iterator();
        while (iterator.hasNext()) {
            MovieDTO movieDTO = iterator.next();
            List<ScheduleDTO> scheduleDTOS = scheduleService.getScheduleByMovieIdAndDate(movieDTO.getMovieID(), schedule).getContent();
            for (ScheduleDTO scheduleDTO : scheduleDTOS) {
                scheduleService.calculateNumOfSeatLeft(scheduleDTO);
            }
            if (scheduleDTOS.isEmpty()) {
                iterator.remove();
            } else {
                movieDTO.setSchedules(scheduleDTOS);
            }
        }
        return movieDTOS;
    }

    public Page<MovieDTO> findMoviesByScheduleAndTheater(LocalDateTime schedule, String theaterName) {
        Theater theater = theaterRepository.findByTheaterNameContainingIgnoreCase(theaterName).getFirst();
        Page<MovieDTO> movieDTOS = movieRepository.findMoviesWithScheduleTodayWithTheater(theater.getTheaterID(), schedule, Pageable.unpaged(), Movie_Status.Active).map(this::toDTO);
        Iterator<MovieDTO> iterator = movieDTOS.iterator();
        while (iterator.hasNext()) {
            MovieDTO movieDTO = iterator.next();
            List<ScheduleDTO> scheduleDTOS = scheduleService.getScheduleByMovieIdAndDate(movieDTO.getMovieID(), schedule).getContent();
            for (ScheduleDTO scheduleDTO : scheduleDTOS) {
                scheduleService.calculateNumOfSeatLeft(scheduleDTO);
            }
            if (scheduleDTOS.isEmpty()) {
                iterator.remove();
            } else {
                movieDTO.setSchedules(scheduleDTOS);
            }
        }
        return movieDTOS;
    }

    @Override
    public List<MovieDTO> getMoviesByActor(String actorName) {
        return movieRepository.findMoviesByActors_ActorNameIgnoreCaseAndStatus(actorName, Movie_Status.Active).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public MovieDTO findMovieByMovieName(String movieName) {
        return toDTO(movieRepository.findMovieByName(movieName));
    }

    @Override
    public List<MovieDTO> findMoviesBooked(Integer customerId) {
        return movieRepository.findMoviesByCustomerId(customerId).stream().map(this::toDTO).collect(Collectors.toList());
    }
}