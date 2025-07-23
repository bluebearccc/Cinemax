package com.bluebear.cinemax.function;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.dto.MovieDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.enumtype.TypeOfRoom;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.theater.TheaterService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class MovieFunction {

    @Autowired
    private MovieService movieService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private GenreService genreService;

    @Tool(description = "Get best movies")
    public MovieDTO getBestMovie() {
        return movieService.findMovieByHighestRate();
    }

    @Tool(description = "Get the hottest movies")
    public List<MovieDTO> getHottestMovies() {
        return movieService.findTop5MoviesHighestRate().getContent();
    }

    @Tool(description = "Get the movies currently showing at specific theater at specific day")
    public List<MovieDTO> getCurrentlyShowingMoviesAtTheater(String theaterName, int dayOffSet) {
        LocalDateTime date = calculateDate( dayOffSet);
        return movieService.findMoviesByScheduleAndTheater(date, theaterName).getContent();
    }

    @Tool(description = "Get the movies currently showing at specific theater at specific day in specific type of room (single or couple)")
    public List<MovieDTO> getCurrentlyShowingMoviesAtTheaterAdnRoom(String theaterName, int dayOffSet, TypeOfRoom roomType) {
        TheaterDTO theaterDTO = theaterService.getTheaterByName(theaterName);
        LocalDateTime date = calculateDate( dayOffSet);
        return movieService.findMoviesByScheduleAndTheaterAndRoomType(date, theaterDTO.getTheaterID(), roomType.name()).getContent();
    }

    @Tool(description = "Get the movies currently showing at specific theater at specific day in specific type of room (single or couple) and movie genre")
    public List<MovieDTO> getCurrentlyShowingMoviesAtTheaterAdnRoomAndGenre(String theaterName, int dayOffSet, TypeOfRoom roomType, String genreName) {
        TheaterDTO theaterDTO = theaterService.getTheaterByName(theaterName);
        LocalDateTime date = calculateDate( dayOffSet);
        GenreDTO genreDTO = genreService.findGenreByName(genreName);
        return movieService.findMoviesByScheduleAndTheaterAndRoomTypeAndGenre(date, theaterDTO.getTheaterID(), roomType.name(), genreName).getContent();
    }

    public LocalDateTime calculateDate(int dayOffSet) {
        if ( dayOffSet == 0 ) {
            return LocalDateTime.now();
        } else {
            return LocalDate.now().plusDays(dayOffSet).atStartOfDay();
        }
    }

    @Tool(description = "Get movies by genre/category")
    public List<MovieDTO> getMoviesByGenre (String genreName) {
        GenreDTO genreDTO = genreService.findGenreByName(genreName);
        return movieService.findMoviesByGenre(genreDTO.getGenreID(), Pageable.unpaged()).getContent();
    }

    @Tool(description = "Get movies by actor")
    public List<MovieDTO> getMoviesByActor (String actorName) {
        return movieService.getMoviesByActor(actorName);
    }

    @Tool(description = "Get movie detail by name")
    public MovieDTO getMovieByName (String movieName) {
        return movieService.findMoviesByName(movieName, Pageable.unpaged()).getContent().get(0);
    }

}
