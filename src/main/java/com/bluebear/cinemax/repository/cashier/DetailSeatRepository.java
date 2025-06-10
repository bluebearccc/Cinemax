package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.DetailSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailSeatRepository extends JpaRepository<DetailSeat, Integer> {
    // Tìm ghế đã đặt theo suất chiếu
    List<DetailSeat> findBySchedule_ScheduleId(Integer scheduleId);

    // Tìm ghế đã đặt theo hóa đơn
    List<DetailSeat> findByInvoice_InvoiceId(Integer invoiceId);

    // Kiểm tra ghế đã được đặt chưa
    boolean existsBySeat_SeatIdAndSchedule_ScheduleId(Integer seatId, Integer scheduleId);

    // Đếm số ghế đã đặt trong suất chiếu
    long countBySchedule_ScheduleId(Integer scheduleId);
}