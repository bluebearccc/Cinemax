package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.MovieActor;
import com.bluebear.cinemax.repository.ActorRepository;
import com.bluebear.cinemax.repository.MovieActorRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class ActorService {

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private MovieActorRepository movieActorRepository;

    @Autowired
    private MovieRepository movieRepository;

    // Lấy tất cả diễn viên
    public List<ActorDTO> getAllActors() {
        return actorRepository.findAllByOrderByActorNameAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy diễn viên theo ID
    public ActorDTO getActorById(Integer id) {
        if (id == null) return null;
        Optional<Actor> actor = actorRepository.findById(id);
        return actor.map(this::convertToDTO).orElse(null);
    }

    // Tìm kiếm diễn viên theo tên
    public List<ActorDTO> searchActorsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllActors();
        }
        return actorRepository.findByActorNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lấy diễn viên theo phim - FIXED
    public List<ActorDTO> getActorsByMovie(Integer movieId) {
        if (movieId == null) return List.of();

        // Sử dụng MovieActorRepository để tìm actorIds theo movieId
        List<Integer> actorIds = movieActorRepository.findactorIDsBymovieID( movieId);

        return actorRepository.findAllById(actorIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Đếm tổng số diễn viên
    public long countAllActors() {
        return actorRepository.count();
    }

    // Convert Entity to DTO - FIXED VERSION
    private ActorDTO convertToDTO(Actor actor) {
        if (actor == null) return null;

        ActorDTO dto = new ActorDTO();
        dto.setActorId((Integer) actor.getActorId()); // Cast Object to Integer
        dto.setActorName(actor.getActorName());
        dto.setImage(actor.getImage());

        // Lấy danh sách phim từ MovieActor relationship - FIXED
        Integer actorId = (Integer) actor.getActorId();
        List<Integer> movieIds = movieActorRepository.findactorIDsBymovieID( actorId);
        List<String> movieNames = new ArrayList<>();

        for (Integer movieId : movieIds) {
            Optional<Movie> movie = movieRepository.findById(movieId);
            if (movie.isPresent()) {
                Movie m = movie.get();
                // Chỉ lấy phim active
                if (m.getStatus() != null && "Active".equals(m.getStatus().toString())) {
                    if (m.getMovieName() != null && !m.getMovieName().trim().isEmpty()) {
                        movieNames.add(m.getMovieName());
                    }
                }
            }
        }

        // Loại bỏ trùng lặp nếu có
        dto.setMovies(movieNames.stream().distinct().collect(Collectors.toList()));

        return dto;
    }
}