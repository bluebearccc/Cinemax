package com.bluebear.cinemax.service.admins;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.GenreRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("genreAdminService")
@Transactional
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    // Basic CRUD Operations
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<GenreDTO> searchGenres(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllGenres();
        }
        return genreRepository.findByGenreNameContainingIgnoreCase(keyword.trim())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public GenreDTO getGenreById(Integer id) {
        return id != null ? genreRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null) : null;
    }

    public List<GenreDTO> getGenresByMovie(Integer movieId) {
        return movieId != null ? genreRepository.findGenresByMovieId(movieId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()) : List.of();
    }

    public boolean existsById(Integer id) {
        return id != null && genreRepository.existsById(id);
    }

    public GenreDTO saveGenre(GenreDTO genreDTO) {
        validateGenreDTO(genreDTO);
        if (isGenreNameExists(genreDTO.getGenreName().trim(), null)) {
            throw new RuntimeException("Genre name already exists!");
        }

        Genre genre = convertToEntity(genreDTO);
        genre.setGenreName(genreDTO.getGenreName().trim());
        return convertToDTO(genreRepository.save(genre));
    }

    public GenreDTO updateGenre(GenreDTO genreDTO) {
        if (genreDTO == null || genreDTO.getGenreID() == null) {
            throw new RuntimeException("Invalid genre data!");
        }

        Genre existingGenre = genreRepository.findById(genreDTO.getGenreID())
                .orElseThrow(() -> new RuntimeException("Genre not found!"));

        validateGenreDTO(genreDTO);
        if (isGenreNameExists(genreDTO.getGenreName().trim(), genreDTO.getGenreID())) {
            throw new RuntimeException("Genre name already exists!");
        }

        existingGenre.setGenreName(genreDTO.getGenreName().trim());
        return convertToDTO(genreRepository.save(existingGenre));
    }

    public boolean deleteGenre(Integer id) {
        if (id == null || !genreRepository.existsById(id)) {
            return false;
        }

        if (getMovieCountByGenreID(id) > 0) {
            throw new RuntimeException("Cannot delete genre that is being used by movies!");
        }

        genreRepository.deleteById(id);
        return true;
    }

    // Validation & Utility Methods
    private void validateGenreDTO(GenreDTO genreDTO) {
        if (genreDTO == null || genreDTO.getGenreName() == null || genreDTO.getGenreName().trim().isEmpty()) {
            throw new RuntimeException("Genre name cannot be empty!");
        }
    }

    public boolean isGenreNameExists(String name, Integer excludeId) {
        if (name == null || name.trim().isEmpty()) return false;

        String trimmedName = name.trim();
        return excludeId == null ?
                genreRepository.existsByGenreNameIgnoreCase(trimmedName) :
                genreRepository.existsByGenreNameIgnoreCaseAndGenreIDNot(trimmedName, excludeId);
    }

    // Statistics Methods
    public long getTotalGenres() {
        return genreRepository.count();
    }

    public long getGenresWithMoviesCount() {
        return genreRepository.countGenresWithMovies();
    }

    public GenreDTO getMostPopularGenre() {
        return genreRepository.findMostPopularGenre()
                .map(this::convertToDTO)
                .orElse(null);
    }

    // Movie Count Methods
    public Long getMovieCountByGenreID(Integer genreId) {
        if (genreId == null) return 0L;
        Long count = genreRepository.countMoviesByGenreID(genreId);
        return count != null ? count : 0L;
    }

    public Map<Integer, Long> getAllGenreMovieCounts() {
        return buildMovieCountMap(genreRepository.findAllGenreMovieCounts());
    }

    public Map<Integer, Long> getGenreMovieCountsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllGenreMovieCounts();
        }
        return buildMovieCountMap(genreRepository.findGenreMovieCountsByKeyword(keyword.trim()));
    }

    private Map<Integer, Long> buildMovieCountMap(List<Object[]> results) {
        Map<Integer, Long> countMap = new HashMap<>();
        for (Object[] row : results) {
            Integer genreId = (Integer) row[0];
            Long movieCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            countMap.put(genreId, movieCount);
        }
        return countMap;
    }

    // Genre-Movie Association Management
    @Transactional
    public void updateGenreMovieAssociations(Integer genreId, String selectedMovieIds) {
        if (genreId == null) {
            throw new RuntimeException("Genre ID cannot be null");
        }

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with ID: " + genreId));

        Set<Integer> selectedMovieIdSet = parseMovieIds(selectedMovieIds);
        Set<Movie> selectedMovies = selectedMovieIdSet.isEmpty() ?
                new HashSet<>() :
                new HashSet<>(movieRepository.findAllById(selectedMovieIdSet));

        updateAssociations(genre, selectedMovies);
        genreRepository.save(genre);
    }

    private Set<Integer> parseMovieIds(String selectedMovieIds) {
        Set<Integer> movieIds = new HashSet<>();
        if (selectedMovieIds != null && !selectedMovieIds.trim().isEmpty()) {
            for (String movieIdStr : selectedMovieIds.split(",")) {
                try {
                    movieIds.add(Integer.parseInt(movieIdStr.trim()));
                } catch (NumberFormatException ignored) {
                    // Skip invalid IDs
                }
            }
        }
        return movieIds;
    }

    private void updateAssociations(Genre genre, Set<Movie> selectedMovies) {
        // Clear existing associations
        if (genre.getMovies() != null) {
            for (Movie movie : new HashSet<>(genre.getMovies())) {
                movie.getGenres().remove(genre);
            }
            genre.getMovies().clear();
        } else {
            genre.setMovies(new ArrayList<>());
        }

        // Add new associations
        for (Movie movie : selectedMovies) {
            if (movie.getGenres() == null) {
                movie.setGenres(new ArrayList<>());
            }
            if (!movie.getGenres().contains(genre)) {
                movie.getGenres().add(genre);
            }
            if (!genre.getMovies().contains(movie)) {
                genre.getMovies().add(movie);
            }
        }
    }

    // Conversion Methods
    private GenreDTO convertToDTO(Genre genre) {
        if (genre == null) return null;
        GenreDTO dto = new GenreDTO();
        dto.setGenreID(genre.getGenreID());
        dto.setGenreName(genre.getGenreName());
        return dto;
    }

    private Genre convertToEntity(GenreDTO dto) {
        if (dto == null) return null;
        Genre genre = new Genre();
        if (dto.getGenreID() != null) {
            genre.setGenreID(dto.getGenreID());
        }
        genre.setGenreName(dto.getGenreName());
        return genre;
    }
}