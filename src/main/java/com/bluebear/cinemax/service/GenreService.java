package com.bluebear.cinemax.service;

import com.bluebear.cinemax.entity.Genre;
import com.bluebear.cinemax.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    // Lấy tất cả thể loại
    public List<Genre> getAllGenres() {
        return genreRepository.findAllByOrderByGenreNameAsc();
    }


    // Lấy thể loại theo ID - Integer version
    public Genre getGenreById(Integer id) {
        if (id == null) return null;
        Optional<Genre> genre = genreRepository.findById(id);
        return genre.orElse(null);
    }


    // Lấy thể loại theo phim
    public List<Genre> getGenresByMovie(Integer movieId) {
        if (movieId == null) return List.of();
        return genreRepository.findByMovieId(movieId);
    }

    // Đếm tổng số thể loại
    public long countAllGenres() {
        return genreRepository.count();
    }

    // Kiểm tra thể loại có tồn tại không
    public boolean existsById(Integer id) {
        if (id == null) return false;
        return genreRepository.existsById(id);
    }

}