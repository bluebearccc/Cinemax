package com.bluebear.cinemax.service.genre;

import com.bluebear.cinemax.dto.GenreDTO;
import com.bluebear.cinemax.entity.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreService {
    // CRUD operations
    GenreDTO createGenre(GenreDTO dto);

    List<GenreDTO> getAllGenres();

    Optional<GenreDTO> getGenreById(Integer id);

    Optional<GenreDTO> updateGenre(Integer id, GenreDTO dto);

    void deleteGenre(Integer id);

    // Optional: conversion methods
    GenreDTO convertToDTO(Genre genre);

    Genre convertToEntity(GenreDTO dto);
}
