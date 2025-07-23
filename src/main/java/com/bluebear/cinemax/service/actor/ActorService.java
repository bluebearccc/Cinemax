package com.bluebear.cinemax.service.actor;

import com.bluebear.cinemax.dto.ActorDTO;
import com.bluebear.cinemax.entity.Actor;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ActorService {
    ActorDTO createActor(ActorDTO actorDTO);

    ActorDTO updateActor(Integer id, ActorDTO actorDTO);

    void deleteActor(Integer id);

    ActorDTO getActorById(Integer id);

    List<ActorDTO> getAllActors();

    Page<ActorDTO> getActorsByMovieId(Integer movieId);

    ActorDTO toDTO(Actor actor);

    Actor toEntity(ActorDTO dto);

}
