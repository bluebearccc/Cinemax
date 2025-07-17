package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.dto.ItemRevenue;
import com.bluebear.cinemax.entity.Detail_FD;
import com.bluebear.cinemax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetailFDRepository extends JpaRepository<Detail_FD, Integer> {
    @Query("SELECT df FROM Detail_FD df WHERE df.invoice = :invoice")
    List<Detail_FD> findByInvoice(Invoice invoice);
    @Query(value = "SELECT * FROM Detail_FD fd WHERE fd.Theater_StockID = :Theater_StockID ", nativeQuery = true)
    public List<Detail_FD> findAllByTheaterStock_TheaterStockId(@Param("Theater_StockID") Integer Theater_StockID);
    @Query("SELECT d FROM Detail_FD d " +
            "JOIN d.invoice i " +
            "JOIN d.theaterStock ts " +
            "WHERE ts.theater.theaterID = :theaterId " +
            "AND i.bookingDate BETWEEN :startDate AND :endDate " +
            "AND i.status = 'COMPLETED'")
    List<Detail_FD> findAllSalesByTheaterAndDateRange(
            @Param("theaterId") Integer theaterId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query(value = "SELECT ts.FoodName AS itemName, SUM(d.TotalPrice) AS totalRevenue " +
            "FROM Detail_FD d " +
            "JOIN Invoice i ON d.InvoiceID = i.InvoiceID " +
            "JOIN Theater_Stock ts ON d.Theater_StockID = ts.Theater_StockID " +
            "WHERE ts.TheaterID = :theaterId " +
            "AND YEAR(i.BookingDate) = :year " +
            "AND MONTH(i.BookingDate) = :month " +
            "AND i.Status = 'Booked' " +
            "GROUP BY ts.FoodName " +
            "ORDER BY totalRevenue DESC",
            nativeQuery = true)
    List<ItemRevenue> findRevenueByItem(
            @Param("theaterId") Integer theaterId,
            @Param("year") int year,
            @Param("month") int month
    );

}
