package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.Invoice_Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    Page<DetailSeat> findBySchedule_ScheduleID(Integer scheduleID , Pageable pageable);

    @Query("SELECT COUNT(d) FROM DetailSeat d JOIN d.schedule s JOIN s.movie m WHERE m.movieID = :movieId")
    long countTotalTicketsSold(int movieId);

    long countBySchedule_ScheduleID(Integer scheduleID);

    @Query("""
        SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END
        FROM DetailSeat ds
        JOIN ds.invoice i
        JOIN ds.schedule s
        WHERE i.customer.id = :customerId
          AND s.movie.movieID = :movieId
          AND i.status = :status
    """)
    boolean hasCustomerWatchedMovie(@Param("customerId") int customerId, @Param("movieId") int movieId, Invoice_Status status);
}