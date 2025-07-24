package com.bluebear.cinemax.service.genre;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public GenreDTO toDTO(Genre genre) {
        return GenreDTO.builder()
                .genreID(genre.getGenreID())
                .genreName(genre.getGenreName())
                .build();
    }

    public Genre toEntity(GenreDTO dto) {
        return Genre.builder()
                .genreID(dto.getGenreID())
                .genreName(dto.getGenreName())
                .build();
    }

    public GenreDTO createGenre(GenreDTO dto) {
        Genre genre = toEntity(dto);
        Genre saved = genreRepository.save(genre);
        return toDTO(saved);
    }

    public GenreDTO findGenreByName(String name) {
        return genreRepository.findByGenreName(name).map(this::toDTO).orElse(null);
    }

    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<GenreDTO> getGenreById(Integer id) {
        return genreRepository.findById(id)
                .map(this::toDTO);
    }

    public Optional<GenreDTO> updateGenre(Integer id, GenreDTO dto) {
        return genreRepository.findById(id).map(existing -> {
            existing.setGenreName(dto.getGenreName());
            Genre updated = genreRepository.save(existing);
            return toDTO(updated);
        });
    }

    public void deleteGenre(Integer id) {
        genreRepository.deleteById(id);
    }
}
