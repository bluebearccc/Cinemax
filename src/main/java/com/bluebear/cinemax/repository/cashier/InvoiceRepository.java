package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    // Tìm hóa đơn theo khách hàng
    List<Invoice> findByCustomer_CustomerId(Integer customerId);

    // Tìm hóa đơn theo nhân viên (thu ngân)
    List<Invoice> findByEmployee_EmployeeId(Integer employeeId);

    // Tìm hóa đơn theo ngày
    List<Invoice> findByBookingDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Thống kê doanh thu theo thu ngân và ngày
    @Query("SELECT SUM(i.totalPrice) FROM Invoice i WHERE i.employee.employeeId = :employeeId AND DATE(i.bookingDate) = :date")
    BigDecimal getTotalRevenueByEmployeeAndDate(@Param("employeeId") Integer employeeId, @Param("date") LocalDate date);

    // Đếm số hóa đơn theo thu ngân
    long countByEmployee_EmployeeId(Integer employeeId);
}
