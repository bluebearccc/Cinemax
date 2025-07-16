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

        // Tìm movie trước, sau đó lấy actors từ relationship
        Optional<Movie> movie = movieRepository.findById(movieId);
        if (movie.isPresent() && movie.get().getActors() != null) {
            return movie.get().getActors().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        return List.of();
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
            Optional<Actor> actorOpt = actorRepository.findById(actorId);
            if (actorOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy diễn viên với ID: " + actorId);
            }

            Actor actor = actorOpt.get();

            // 1. Xóa actor khỏi tất cả movies hiện tại
            if (actor.getMovies() != null) {
                for (Movie movie : new ArrayList<>(actor.getMovies())) {
                    if (movie.getActors() != null) {
                        movie.getActors().remove(actor);
                        movieRepository.save(movie);
                    }
                }
            }

            // 2. Thêm actor vào movies mới
            if (movieIds != null && !movieIds.isEmpty()) {
                for (Integer movieId : movieIds) {
                    Optional<Movie> movieOpt = movieRepository.findById(movieId);
                    if (movieOpt.isPresent()) {
                        Movie movie = movieOpt.get();
                        if (movie.getActors() == null) {
                            movie.setActors(new ArrayList<>());
                        }
                        movie.getActors().add(actor);
                        movieRepository.save(movie);
                    }
                }
            }

            System.out.println("Successfully updated actor movies");

        } catch (Exception e) {
            System.err.println("Error updating actor movies: " + e.getMessage());
            throw new RuntimeException("Không thể cập nhật danh sách phim: " + e.getMessage());
        }
    }

        public void deleteActor (Integer actorId){
            if (actorId == null) {
                throw new IllegalArgumentException("Actor ID không được null");
            }

            try {
                // Kiểm tra actor có tồn tại không
                Optional<Actor> actorOpt = actorRepository.findById(actorId);
                if (actorOpt.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy diễn viên với ID: " + actorId);
                }

                Actor actor = actorOpt.get();

                // Xóa actor khỏi tất cả movies trước khi xóa actor
                if (actor.getMovies() != null) {
                    for (Movie movie : new ArrayList<>(actor.getMovies())) {
                        if (movie.getActors() != null) {
                            movie.getActors().remove(actor);
                            movieRepository.save(movie);
                        }
                    }
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
        public long countAllActors () {
            return actorRepository.count();
        }

        // Convert Entity to DTO - FIXED VERSION
        private ActorDTO convertToDTO (Actor actor){
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
                        try {
                            actorId = Integer.parseInt(actorIdObj.toString());
                        } catch (NumberFormatException e) {
                            System.err.println("Cannot convert actorId to Integer: " + actorIdObj);
                            return null;
                        }
                    }
                }

                dto.setActorId(actorId);
                dto.setActorName(actor.getActorName());
                dto.setImage(actor.getImage());

                // SIMPLIFIED: Lấy danh sách phim trực tiếp từ relationship
                List<String> movieNames = new ArrayList<>();
                if (actor.getMovies() != null) {
                    for (Movie movie : actor.getMovies()) {
                        // Chỉ lấy phim active
                        if (movie.getStatus() != null && "Active".equals(movie.getStatus().toString())) {
                            if (movie.getMovieName() != null && !movie.getMovieName().trim().isEmpty()) {
                                movieNames.add(movie.getMovieName());
                            }
                        }
                    }
                }

                // Loại bỏ trùng lặp nếu có
                dto.setMovies(movieNames.stream().distinct().collect(Collectors.toList()));

            } catch (Exception e) {
                System.err.println("Error converting Actor to DTO: " + e.getMessage());
                e.printStackTrace();
                return null;
            }

            return dto;
        }
    }
