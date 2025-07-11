package com.bluebear.cinemax;

import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CinemaxApplication {
    private static final Logger logger = LoggerFactory.getLogger(CinemaxApplication.class);

    @Autowired
    private MovieRepository movieRepository;

    public static void main(String[] args) {
        SpringApplication.run(CinemaxApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner displayMovies() {
//        return args -> {
//            try {
//                // Fetch all movies from the database
//                Iterable<Movie> movies = movieRepository.findAll();
//
//                // Check if there are any movies
//                if (!movies.iterator().hasNext()) {
//                    logger.info("No movies found in the database.");
//                    return;
//                }
//
//                // Display the list of movies
//                logger.info("List of Movies:");
//                logger.info("--------------------------------------------------");
//                for (Movie movie : movies) {
//                    logger.info("ID: {}", movie.getId());
//                    logger.info("Name: {}", movie.getMovieName());
//                    logger.info("Description: {}", movie.getDescription());
//                    logger.info("Genre: {}", movie.getGenre());
//                    logger.info("Studio: {}", movie.getStudio());
//                    logger.info("Duration: {} minutes", movie.getDuration());
//                    logger.info("Rating: {}", movie.getMovieRate());
//                    logger.info("Actors: {}", movie.getActor());
//                    logger.info("Image URL: {}", movie.getImageUrl());
//                    logger.info("Trailer URL: {}", movie.getTrailerUrl());
//                    logger.info("--------------------------------------------------");
//                }
//            } catch (Exception e) {
//                logger.error("Error retrieving movies: {}", e.getMessage());
//                throw new RuntimeException("Failed to retrieve movies", e);
//            }
//        };
//    }
}
