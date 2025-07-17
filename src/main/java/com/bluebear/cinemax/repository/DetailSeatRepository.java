package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bluebear.cinemax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    List<DetailSeat> findByInvoiceInvoiceID(Integer invoiceId);
    List<DetailSeat> findByInvoiceInvoiceIDAndStatus(Integer invoice_invoiceID, DetailSeat_Status status);
    @Query("SELECT ds.seat.seatID FROM DetailSeat ds WHERE ds.schedule.scheduleID = :scheduleId")
    List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId);
    Page<DetailSeat> findBySchedule_ScheduleID(Integer scheduleID , Pageable pageable);

    @Query("SELECT COUNT(d) FROM DetailSeat d JOIN d.schedule s JOIN s.movie m WHERE m.movieID = :movieId")
    long countTotalTicketsSold(int movieId);

    long countBySchedule_ScheduleID(Integer scheduleID);

    @Query("SELECT ds FROM DetailSeat ds WHERE ds.invoice = :invoice")
    List<DetailSeat> findByInvoice(Invoice invoice);


    List<DetailSeat> findBySeatSeatIDAndScheduleScheduleIDAndStatusIn(Integer seatId, Integer scheduleId, List<DetailSeat_Status> statuses);


    boolean existsBySeatSeatIDAndScheduleScheduleID(Integer seatId, Integer scheduleId);
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Detail_Seat (invoiceId, seatId, scheduleId ,status) VALUES (:invoiceId, :seatId, :scheduleId, :status)", nativeQuery = true)
    void insertDetailSeat(@Param("invoiceId") int invoiceId,
                          @Param("seatId") int seatId,
                          @Param("scheduleId") int scheduleId,
                          @Param("status")String status);
    boolean existsBySeatSeatIDAndScheduleScheduleIDAndStatus(Integer seatId, Integer scheduleId, DetailSeat_Status status);
    @Modifying
    @Transactional
    @Query("UPDATE DetailSeat ds SET ds.status = :status WHERE ds.invoice.invoiceID = :invoiceId")
    void updateStatusByInvoiceId(@Param("invoiceId") int invoiceId,
                                 @Param("status") com.bluebear.cinemax.enumtype.DetailSeat_Status status);
    boolean existsBySeatSeatIDAndScheduleScheduleIDAndStatusIn(Integer seatId, Integer scheduleId, List<DetailSeat_Status> statuses);

    @Query("SELECT COUNT(ds) FROM DetailSeat ds " +
            "WHERE ds.invoice.status = 'Booked' AND ds.invoice.bookingDate BETWEEN :startOfDay AND :endOfDay")
    Long countTicketsToday(@Param("startOfDay") LocalDateTime startOfDay,
                           @Param("endOfDay") LocalDateTime endOfDay);


    // Tổng số vé đã bán trong khoảng thời gian
    @Query("SELECT COUNT(ds) FROM DetailSeat ds " +
            "WHERE ds.invoice.status = 'Booked' AND ds.invoice.bookingDate BETWEEN :start AND :end")
    Integer countTicketsBetween(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}