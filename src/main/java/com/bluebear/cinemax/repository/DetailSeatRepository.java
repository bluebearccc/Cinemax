package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bluebear.cinemax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    @Query("SELECT ds.seat.seatID FROM DetailSeat ds WHERE ds.schedule.scheduleID = :scheduleId")
    List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId);
    Page<DetailSeat> findBySchedule_ScheduleID(Integer scheduleID , Pageable pageable);

    @Query("SELECT COUNT(d) FROM DetailSeat d JOIN d.schedule s JOIN s.movie m WHERE m.movieID = :movieId")
    long countTotalTicketsSold(int movieId);

    long countBySchedule_ScheduleID(Integer scheduleID);

    @Query("SELECT ds FROM DetailSeat ds WHERE ds.invoice = :invoice")
    List<DetailSeat> findByInvoice(Invoice invoice);
}