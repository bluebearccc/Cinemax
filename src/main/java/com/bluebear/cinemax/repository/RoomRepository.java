package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.enumtype.Room_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    List<Room> findAllByTheater_TheaterID(Integer theaterID);

    List<Room> findByStatus(Room_Status status);

    List<Room> findByTheater_TheaterIDAndStatus(Integer theaterID, Room_Status status);
    boolean existsByNameAndTheater_TheaterID(String name, Integer theaterID);
    Optional<Room> findTopByTheater_TheaterIDAndNameStartsWithOrderByNameDesc(Integer theaterId, String prefix);
    long countByTheater_TheaterID(Integer theaterId);

}
