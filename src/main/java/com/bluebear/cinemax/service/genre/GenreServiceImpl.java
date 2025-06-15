package com.bluebear.cinemax.service.genre;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.repository.GenreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public GenreDTO convertToDTO(Genre genre) {
        return GenreDTO.builder()
                .genreID(genre.getGenreID())
                .genreName(genre.getGenreName())
                .build();
    }

    public Genre convertToEntity(GenreDTO dto) {
        return Genre.builder()
                .genreID(dto.getGenreID())
                .genreName(dto.getGenreName())
                .build();
    }

    @Transactional
    public GenreDTO createGenre(GenreDTO dto) {
        Genre genre = convertToEntity(dto);
        Genre saved = genreRepository.save(genre);
        return convertToDTO(saved);
    }

    @Transactional
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<GenreDTO> getGenreById(Integer id) {
        return genreRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public Optional<GenreDTO> updateGenre(Integer id, GenreDTO dto) {
        return genreRepository.findById(id).map(existing -> {
            existing.setGenreName(dto.getGenreName());
            Genre updated = genreRepository.save(existing);
            return convertToDTO(updated);
        });
    }

    @Transactional
    public void deleteGenre(Integer id) {
        genreRepository.deleteById(id);
    }
}
