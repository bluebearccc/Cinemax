package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    @Query("""
    SELECT r FROM Room r
    WHERE r.theater.theaterId = :theaterId
    AND r.status = :status
    """)
    List<Room> findByTheaterIdAndStatus(@Param("theaterId") Integer theaterId,
                                        @Param("status") Room.RoomStatus status);
}