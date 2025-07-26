package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByCustomerAndStatus(Customer customer, InvoiceStatus invoiceStatus);

    boolean existsByCustomerAndStatus(Customer customer, InvoiceStatus invoiceStatus);
    List<Invoice> findByStatusAndBookingDateBefore(InvoiceStatus status, LocalDateTime time);
    @Query("SELECT SUM(i.totalPrice) FROM Invoice i " +
            "WHERE i.status = 'Booked' AND i.bookingDate BETWEEN :startOfDay AND :endOfDay")
    Double getTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                           @Param("endOfDay") LocalDateTime endOfDay);

    // Tổng doanh thu trong khoảng thời gian bất kỳ
    @Query("SELECT SUM(i.totalPrice) FROM Invoice i " +
            "WHERE i.status = 'Booked' AND i.bookingDate BETWEEN :start AND :end")
    Double getRevenueBetween(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);
    @Query(value = "SELECT CAST(BookingDate AS DATE) AS booking_day, SUM(TotalPrice) AS total " +
            "FROM Invoice " +
            "WHERE BookingDate BETWEEN :start AND :end AND status ='Booked' " +
            "GROUP BY CAST(BookingDate AS DATE) " +
            "ORDER BY CAST(BookingDate AS DATE)",
            nativeQuery = true)
    List<Object[]> getRevenueByBookingDate(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("""
    SELECT DISTINCT i FROM Invoice i
    JOIN i.detailSeats ds
    JOIN ds.schedule s
    WHERE i.status = 'Booked'
      AND s.endTime BETWEEN :start AND :end
""")
    List<Invoice> findInvoicesInFeedbackWindow(LocalDateTime start, LocalDateTime end);
    @Query(value = "SELECT DISTINCT  i.* FROM Invoice i " +
            "JOIN Detail_Seat ds ON i.InvoiceID = ds.InvoiceID " +
            "WHERE ds.ScheduleID = :scheduleId " +
            "AND ds.ScheduleID IN (SELECT s.ScheduleID FROM Schedule s WHERE s.StartTime >= CURRENT_TIMESTAMP)",
            nativeQuery = true)
    List<Invoice> findActiveBookingsForSchedule(@Param("scheduleId") Integer scheduleId);
    @Query("SELECT DISTINCT i FROM Invoice i " +
            "JOIN i.detailSeats ds " +
            "JOIN ds.schedule s " +
            "JOIN s.room r " +
            "WHERE r.theater.theaterID = :theaterId AND s.startTime >= :now")
    List<Invoice> findActiveBookingsForTheater(@Param("theaterId") Integer theaterId, @Param("now") LocalDateTime now);
    @Query("SELECT DISTINCT i FROM Invoice i " +
            "JOIN i.detailSeats ds " +
            "JOIN ds.schedule s " +
            "WHERE s.room.roomID = :roomId AND s.startTime >= :now")
    List<Invoice> findActiveBookingsForRoom(@Param("roomId") Integer roomId, @Param("now") LocalDateTime now);
    @Query("SELECT DISTINCT i FROM Invoice i " +
            "JOIN i.detail_FD dfd " +         // Join đến chi tiết đồ ăn
            "JOIN i.detailSeats ds " +       // Join đến chi tiết vé để lấy thông tin lịch chiếu
            "WHERE dfd.theaterStock.stockID = :stockId " +
            "AND ds.schedule.startTime >= :now")
    List<Invoice> findActiveInvoicesByTheaterStockId(@Param("stockId") Integer stockId, @Param("now") LocalDateTime now);
}
