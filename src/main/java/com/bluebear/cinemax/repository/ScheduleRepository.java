package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByMovie_MovieID(Integer movieId);

    @Query(value = "SELECT * FROM Schedule s WHERE s.MovieID = :movieId AND s.RoomID = :roomId AND CAST(s.EndTime AS DATE) = :date", nativeQuery = true)
    List<Schedule> findAllByMovieIdAndRoomId(@Param("movieId") Integer movieId, @Param("roomId") Integer roomId, @Param("date") LocalDate date);

    @Query(value = "Select r.RoomID\n" +
            "from Room r \n" +
            "where r.TheaterID = :theaterId and r.Status = 'Active' \n" +
            "Except\n" +
            "SELECT DISTINCT\n" +
            "            S.RoomID\n" +
            "        FROM\n" +
            "            dbo.Schedule AS S\n" +
            "        WHERE\n" +
            "            S.StartTime < :endTime AND S.EndTime > :startTime And s.Status = 'Active'", nativeQuery = true)
    List<String> findAvailableRooms(@Param("theaterId") Integer theaterId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    List<Schedule> findByStatus(Schedule_Status status);

    @Query("SELECT s FROM Movie m JOIN m.scheduleList s WHERE m.movieID = :movieId AND CAST(s.startTime AS DATE) = CAST(:day AS DATE) AND s.status = :status")
    List<Schedule> findSchedulesByMovie_MovieIDInTodayAndStatus(int movieId, LocalDateTime day, Schedule_Status status);

}
