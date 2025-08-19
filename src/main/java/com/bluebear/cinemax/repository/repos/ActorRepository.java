package com.bluebear.cinemax.repository.repos;

import com.bluebear.cinemax.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("actorrepoadmin")
public interface ActorRepository extends JpaRepository<Actor, Integer> {

 //    Tìm diễn viên theo tên (tìm kiếm không phân biệt hoa thường)

    List<Actor> findByActorNameContainingIgnoreCase(String actorName);

}