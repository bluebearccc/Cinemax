package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.entity.MovieActor;
import com.bluebear.cinemax.repository.ActorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActorService {

    @Autowired
    private ActorRepository actorRepository;

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

    // Lấy diễn viên theo phim
    public List<ActorDTO> getActorsByMovie(Integer movieId) {
        if (movieId == null) return List.of();
        return actorRepository.findByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Đếm tổng số diễn viên
    public long countAllActors() {
        return actorRepository.count();
    }

    // Convert Entity to DTO
    private ActorDTO convertToDTO(Actor actor) {
        if (actor == null) return null;

        ActorDTO dto = new ActorDTO();
        dto.setActorId(actor.getActorId());
        dto.setActorName(actor.getActorName());
        dto.setImage(actor.getImage());

        // Lấy danh sách phim - null check và filter active movies
        if (actor.getMovieActors() != null && !actor.getMovieActors().isEmpty()) {
            List<String> movies = actor.getMovieActors().stream()
                    .filter(ma -> ma.getMovie() != null)
                    .filter(ma -> ma.getMovie().getStatus() != null)
                    .filter(ma -> ma.getMovie().getStatus().toString().equals("Active")) // Chỉ lấy phim active
                    .map(ma -> ma.getMovie().getMovieName())
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .distinct() // Loại bỏ trùng lặp
                    .collect(Collectors.toList());
            dto.setMovies(movies);
        }

        return dto;
    }
}