package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    // Find voucher by promotion code
    Optional<Voucher> findByPromotionCode(String promotionCode);

    // Check if promotion code exists (for validation)
    boolean existsByPromotionCode(String promotionCode);

    // Find vouchers by status
    List<Voucher> findByStatus(String status);

    // Find active vouchers
    @Query("SELECT v FROM Voucher v WHERE v.status = 'Active' AND v.startTime <= :now AND v.endTime >= :now AND v.quantity > 0")
    List<Voucher> findActiveVouchers(@Param("now") LocalDateTime now);

    // Find expired vouchers
    @Query("SELECT v FROM Voucher v WHERE v.endTime < :now")
    List<Voucher> findExpiredVouchers(@Param("now") LocalDateTime now);

    // Search vouchers by code or status
    @Query("SELECT v FROM Voucher v WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(v.promotionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR v.status = :status)")
    List<Voucher> searchVouchers(@Param("keyword") String keyword, @Param("status") String status);

    // Count vouchers by status
    long countByStatus(String status);

    // Find vouchers ending soon (within next 7 days)
    @Query("SELECT v FROM Voucher v WHERE v.status = 'Active' AND v.endTime BETWEEN :now AND :weekLater")
    List<Voucher> findVouchersEndingSoon(@Param("now") LocalDateTime now, @Param("weekLater") LocalDateTime weekLater);

    // Get voucher statistics
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.status = :status")
    long countVouchersByStatus(@Param("status") String status);

    @Query("SELECT AVG(v.discount) FROM Voucher v WHERE v.status = 'Active'")
    Double getAverageDiscount();
}