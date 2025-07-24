package com.bluebear.cinemax.service.actor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.entity.Actor;
import com.bluebear.cinemax.repository.ActorRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ActorServiceImpl implements ActorService{

    @Autowired
    private ActorRepository actorRepository;

    public ActorDTO createActor(ActorDTO actorDTO) {
        Actor actor = toEntity(actorDTO);
        return toDTO(actorRepository.save(actor));
    }

    public ActorDTO updateActor(Integer id, ActorDTO actorDTO) {
        Optional<Actor> optional = actorRepository.findById(id);
        if (optional.isPresent()) {
            Actor actor = optional.get();
            actor.setActorName(actorDTO.getActorName());
            actor.setImage(actorDTO.getImage());
            return toDTO(actorRepository.save(actor));
        }
        return null;
    }

    public void deleteActor(Integer id) {
        actorRepository.deleteById(id);
    }

    public ActorDTO getActorById(Integer id) {
        return actorRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }
    
    public List<ActorDTO> getAllActors() {
        return actorRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public Page<ActorDTO> getActorsByMovieId(Integer movieId) {
        return actorRepository.findActorsByMovieId(movieId, Pageable.unpaged()).map(this::toDTO);
    }

    public ActorDTO toDTO(Actor actor) {
        ActorDTO dto = new ActorDTO();
        dto.setActorID(actor.getActorID());
        dto.setActorName(actor.getActorName());
        dto.setImage(actor.getImage());
        return dto;
    }

    public Actor toEntity(ActorDTO dto) {
        Actor actor = new Actor();
        actor.setActorID(dto.getActorID());
        actor.setActorName(dto.getActorName());
        actor.setImage(dto.getImage());
        return actor;
    }


}
