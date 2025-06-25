package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public List<GenreDTO> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genres.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private GenreDTO convertToDTO(Genre genre) {
        // Assuming GenreDTO has similar fields to Genre entity
        // You'll need to implement this conversion based on your GenreDTO structure
        GenreDTO dto = new GenreDTO();
        dto.setGenreID(genre.getGenreID());
        dto.setGenreName(genre.getGenreName());
        // Add other field mappings as needed
        return dto;
    }
}