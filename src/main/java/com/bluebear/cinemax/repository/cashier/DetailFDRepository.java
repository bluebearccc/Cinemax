package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.DetailFD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetailFDRepository extends JpaRepository<DetailFD, Integer> {
    // Tìm chi tiết đồ ăn theo hóa đơn
    List<DetailFD> findByInvoice_InvoiceId(Integer invoiceId);

    // Thống kê doanh thu đồ ăn theo rạp và ngày
    @Query("SELECT SUM(df.totalPrice) FROM DetailFD df WHERE df.theaterStock.theater.theaterId = :theaterId AND DATE(df.invoice.bookingDate) = :date")
    BigDecimal getTotalFoodRevenueByTheaterAndDate(@Param("theaterId") Integer theaterId, @Param("date") LocalDate date);

    // Thống kê món bán chạy
    @Query("SELECT df.theaterStock.foodName, SUM(df.quantity) FROM DetailFD df WHERE df.theaterStock.theater.theaterId = :theaterId GROUP BY df.theaterStock.theaterStockId ORDER BY SUM(df.quantity) DESC")
    List<Object[]> getTopSellingFoodByTheater(@Param("theaterId") Integer theaterId);
}