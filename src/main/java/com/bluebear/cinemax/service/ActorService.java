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

    // Lấy tất cả diễn viên - FIXED: Sắp xếp theo ID thay vì tên
    public List<ActorDTO> getAllActors() {
        return actorRepository.findAll().stream()
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
        List<Integer> actorIds = movieActorRepository.findactorIDsBymovieID(movieId);

        return actorRepository.findAllById(actorIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // FIXED: Thêm method để lưu actor mới
    public ActorDTO saveActor(ActorDTO actorDTO) {
        if (actorDTO == null) return null;

        try {
            Actor actor = new Actor();
            actor.setActorName(actorDTO.getActorName());
            actor.setImage(actorDTO.getImage());

            Actor savedActor = actorRepository.save(actor);
            return convertToDTO(savedActor);
        } catch (Exception e) {
            System.err.println("Error saving actor: " + e.getMessage());
            throw new RuntimeException("Không thể lưu diễn viên: " + e.getMessage());
        }
    }

    // FIXED: Thêm method để cập nhật actor
    public ActorDTO updateActor(ActorDTO actorDTO) {
        if (actorDTO == null || actorDTO.getActorId() == null) return null;

        try {
            Optional<Actor> existingActor = actorRepository.findById(actorDTO.getActorId());
            if (existingActor.isEmpty()) {
                throw new RuntimeException("Không tìm thấy diễn viên với ID: " + actorDTO.getActorId());
            }

            Actor actor = existingActor.get();
            actor.setActorName(actorDTO.getActorName());

            // Chỉ cập nhật ảnh nếu có ảnh mới
            if (actorDTO.getImage() != null && !actorDTO.getImage().trim().isEmpty()) {
                actor.setImage(actorDTO.getImage());
            }

            Actor updatedActor = actorRepository.save(actor);
            return convertToDTO(updatedActor);
        } catch (Exception e) {
            System.err.println("Error updating actor: " + e.getMessage());
            throw new RuntimeException("Không thể cập nhật diễn viên: " + e.getMessage());
        }
    }

    // FIXED: Thêm method để cập nhật quan hệ Actor-Movie
    public void updateActorMovies(Integer actorId, List<Integer> movieIds) {
        if (actorId == null) return;

        try {
            // Xóa tất cả quan hệ cũ của actor này
            List<MovieActor> existingRelations = movieActorRepository.findByactorID(actorId);
            if (!existingRelations.isEmpty()) {
                movieActorRepository.deleteAll(existingRelations);
            }

            // Thêm quan hệ mới
            if (movieIds != null && !movieIds.isEmpty()) {
                Optional<Actor> actor = actorRepository.findById(actorId);
                if (actor.isPresent()) {
                    for (Integer movieId : movieIds) {
                        Optional<Movie> movie = movieRepository.findById(movieId);
                        if (movie.isPresent()) {
                            MovieActor movieActor = new MovieActor();
                            movieActor.setActor(actor.get());
                            movieActor.setMovie(movie.get());
                            movieActorRepository.save(movieActor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating actor movies: " + e.getMessage());
            throw new RuntimeException("Không thể cập nhật danh sách phim: " + e.getMessage());
        }
    }

    public void deleteActor(Integer actorId) {
        if (actorId == null) {
            throw new IllegalArgumentException("Actor ID không được null");
        }

        try {
            // Kiểm tra actor có tồn tại không
            Optional<Actor> actor = actorRepository.findById(actorId);
            if (actor.isEmpty()) {
                throw new RuntimeException("Không tìm thấy diễn viên với ID: " + actorId);
            }

            // Xóa tất cả quan hệ MovieActor trước
            List<MovieActor> movieActors = movieActorRepository.findByactorID(actorId);
            if (!movieActors.isEmpty()) {
                movieActorRepository.deleteAll(movieActors);
            }

            // Xóa actor
            actorRepository.deleteById(actorId);

            System.out.println("Successfully deleted actor with ID: " + actorId);

        } catch (Exception e) {
            System.err.println("Error deleting actor: " + e.getMessage());
            throw new RuntimeException("Không thể xóa diễn viên: " + e.getMessage());
        }
    }

    // Đếm tổng số diễn viên
    public long countAllActors() {
        return actorRepository.count();
    }

    // Convert Entity to DTO - FIXED VERSION
    private ActorDTO convertToDTO(Actor actor) {
        if (actor == null) return null;

        ActorDTO dto = new ActorDTO();

        try {
            // FIXED: Kiểm tra null và cast an toàn
            Object actorIdObj = actor.getActorId();
            Integer actorId = null;

            if (actorIdObj != null) {
                if (actorIdObj instanceof Integer) {
                    actorId = (Integer) actorIdObj;
                } else if (actorIdObj instanceof Number) {
                    actorId = ((Number) actorIdObj).intValue();
                } else {
                    // Thử parse từ String
                    try {
                        actorId = Integer.parseInt(actorIdObj.toString());
                    } catch (NumberFormatException e) {
                        System.err.println("Cannot convert actorId to Integer: " + actorIdObj);
                        return null; // Hoặc bỏ qua actor này
                    }
                }
            }

            dto.setActorId(actorId);

            // DEBUG: Kiểm tra actorName trước khi set
            String actorName = actor.getActorName();
            System.out.println("DEBUG - Raw actorName from entity: [" + actorName + "]");
            System.out.println("DEBUG - ActorName is null: " + (actorName == null));
            System.out.println("DEBUG - ActorName is empty: " + (actorName != null && actorName.isEmpty()));
            System.out.println("DEBUG - ActorName length: " + (actorName != null ? actorName.length() : "null"));

            dto.setActorName(actorName);
            dto.setImage(actor.getImage());

            // DEBUG: Kiểm tra sau khi set vào DTO
            System.out.println("DEBUG - DTO actorName after set: [" + dto.getActorName() + "]");
            System.out.println("Converting Actor - ID: " + actorId + ", Name: " + actorName);

            // FIXED: Lấy danh sách phim theo actorId
            if (actorId != null) {
                List<Integer> movieIds = movieActorRepository.findmovieIDsByactorID(actorId);
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
            } else {
                dto.setMovies(new ArrayList<>());
            }

        } catch (Exception e) {
            System.err.println("Error converting Actor to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return dto;
    }
}