package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer> {

 //    Tìm diễn viên theo tên (tìm kiếm không phân biệt hoa thường)

    List<Actor> findByActorNameContainingIgnoreCase(String actorName);

}