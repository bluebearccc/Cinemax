package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.repository.ActorRepository;
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
    private MovieRepository movieRepository;

    public List<ActorDTO> getAllActors() {
        return actorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ActorDTO getActorById(Integer id) {
        return id == null ? null :
                actorRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    public List<ActorDTO> searchActorsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllActors();
        }
        return actorRepository.findByActorNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ActorDTO> getActorsByMovie(Integer movieId) {
        if (movieId == null) return List.of();

        return movieRepository.findById(movieId)
                .map(movie -> movie.getActors() != null ?
                        movie.getActors().stream().map(this::convertToDTO).collect(Collectors.toList()) :
                        List.<ActorDTO>of())
                .orElse(List.of());
    }

    public ActorDTO saveActor(ActorDTO actorDTO) {
        if (actorDTO == null) return null;

        Actor actor = new Actor();
        actor.setActorName(actorDTO.getActorName());

        // Handle image path - use provided path or default
        String imagePath = actorDTO.getImage();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imagePath = "/images/default-actor.png";
        }
        actor.setImage(imagePath);

        return convertToDTO(actorRepository.save(actor));
    }

    public ActorDTO updateActor(ActorDTO actorDTO) {
        if (actorDTO == null || actorDTO.getActorId() == null) return null;

        return actorRepository.findById(actorDTO.getActorId())
                .map(actor -> {
                    actor.setActorName(actorDTO.getActorName());

                    // Handle image path - use provided path or keep existing or use default
                    String imagePath = actorDTO.getImage();
                    if (imagePath == null || imagePath.trim().isEmpty()) {
                        // If no new image path provided, keep existing or use default
                        if (actor.getImage() == null || actor.getImage().trim().isEmpty()) {
                            imagePath = "/images/default-actor.png";
                        } else {
                            imagePath = actor.getImage(); // Keep existing
                        }
                    }
                    actor.setImage(imagePath);

                    return convertToDTO(actorRepository.save(actor));
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn viên với ID: " + actorDTO.getActorId()));
    }

    public void updateActorMovies(Integer actorId, List<Integer> movieIds) {
        if (actorId == null) return;

        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn viên với ID: " + actorId));

        // Xóa actor khỏi tất cả movies hiện tại
        Optional.ofNullable(actor.getMovies())
                .orElse(new ArrayList<>())
                .forEach(movie -> {
                    if (movie.getActors() != null) {
                        movie.getActors().remove(actor);
                        movieRepository.save(movie);
                    }
                });

        // Thêm actor vào movies mới
        Optional.ofNullable(movieIds)
                .orElse(new ArrayList<>())
                .stream()
                .map(movieRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(movie -> {
                    if (movie.getActors() == null) {
                        movie.setActors(new ArrayList<>());
                    }
                    movie.getActors().add(actor);
                    movieRepository.save(movie);
                });
    }

    public void deleteActor(Integer actorId) {
        if (actorId == null) {
            throw new IllegalArgumentException("Actor ID không được null");
        }

        Actor actor = actorRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn viên với ID: " + actorId));

        // Xóa actor khỏi tất cả movies
        Optional.ofNullable(actor.getMovies())
                .orElse(new ArrayList<>())
                .forEach(movie -> {
                    if (movie.getActors() != null) {
                        movie.getActors().remove(actor);
                        movieRepository.save(movie);
                    }
                });

        actorRepository.deleteById(actorId);
    }

    public long countAllActors() {
        return actorRepository.count();
    }

    private ActorDTO convertToDTO(Actor actor) {
        if (actor == null) return null;

        ActorDTO dto = new ActorDTO();
        dto.setActorId(convertToInteger(actor.getActorId()));
        dto.setActorName(actor.getActorName());

        // Handle image path - ensure it's not null
        String imagePath = actor.getImage();
        if (imagePath == null || imagePath.trim().isEmpty()) {
            imagePath = "/images/default-actor.png";
        }
        dto.setImage(imagePath);

        // Lấy danh sách phim active
        List<String> movieNames = Optional.ofNullable(actor.getMovies())
                .orElse(new ArrayList<>())
                .stream()
                .filter(movie -> "Active".equals(String.valueOf(movie.getStatus())))
                .map(Movie::getMovieName)
                .filter(name -> name != null && !name.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        dto.setMovies(movieNames);
        return dto;
    }

    private Integer convertToInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();

        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            System.err.println("Cannot convert to Integer: " + obj);
            return null;
        }
    }
}