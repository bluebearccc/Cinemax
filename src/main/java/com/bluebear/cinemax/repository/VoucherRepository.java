package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Promotion;
import com.bluebear.cinemax.enumtype.Promotion_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Promotion, Integer> {

    // Find voucher by promotion code
    Optional<Promotion> findByPromotionCode(String promotionCode);

    // Check if promotion code exists (for validation)
    boolean existsByPromotionCode(String promotionCode);

    // Find vouchers by status (using enum)
    List<Promotion> findByStatus(Promotion_Status status);

    // Find active vouchers
    @Query("SELECT v FROM Promotion v WHERE v.status = :status AND v.startTime <= :now AND v.endTime >= :now AND v.quantity > 0")
    List<Promotion> findActiveVouchers(@Param("status") Promotion_Status status, @Param("now") LocalDateTime now);

    // Find expired vouchers
    @Query("SELECT v FROM Promotion v WHERE v.endTime < :now OR v.status = :expiredStatus")
    List<Promotion> findExpiredVouchers(@Param("now") LocalDateTime now, @Param("expiredStatus") Promotion_Status expiredStatus);

    // Search vouchers by code or status
    @Query("SELECT v FROM Promotion v WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(v.promotionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR v.status = :status)")
    List<Promotion> searchVouchers(@Param("keyword") String keyword, @Param("status") Promotion_Status status);

    // Count vouchers by status (using enum)
    long countByStatus(Promotion_Status status);

    // Find vouchers ending soon (within next 7 days)
    @Query("SELECT v FROM Promotion v WHERE v.status = :status AND v.endTime BETWEEN :now AND :weekLater")
    List<Promotion> findVouchersEndingSoon(@Param("status") Promotion_Status status, @Param("now") LocalDateTime now, @Param("weekLater") LocalDateTime weekLater);

    // Get voucher statistics - count by specific status
    @Query("SELECT COUNT(v) FROM Promotion v WHERE v.status = :status")
    long countVouchersByStatus(@Param("status") Promotion_Status status);

    // Get average discount for active vouchers
    @Query("SELECT AVG(v.discount) FROM Promotion v WHERE v.status = :status")
    Double getAverageDiscount(@Param("status") Promotion_Status status);

    // Additional useful methods

    // Find vouchers by date range
    @Query("SELECT v FROM Promotion v WHERE v.startTime >= :startDate AND v.endTime <= :endDate")
    List<Promotion> findVouchersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find vouchers by discount range
    @Query("SELECT v FROM Promotion v WHERE v.discount BETWEEN :minDiscount AND :maxDiscount")
    List<Promotion> findVouchersByDiscountRange(@Param("minDiscount") Double minDiscount, @Param("maxDiscount") Double maxDiscount);

    // Find vouchers with quantity greater than specified value
    @Query("SELECT v FROM Promotion v WHERE v.quantity > :minQuantity")
    List<Promotion> findVouchersWithQuantityGreaterThan(@Param("minQuantity") Integer minQuantity);

    // Find vouchers starting soon
    @Query("SELECT v FROM Promotion v WHERE v.startTime BETWEEN :now AND :futureTime AND v.status = :status")
    List<Promotion> findVouchersStartingSoon(@Param("now") LocalDateTime now, @Param("futureTime") LocalDateTime futureTime, @Param("status") Promotion_Status status);

    // Get total value of all active vouchers
    @Query("SELECT SUM(v.discount * v.quantity) FROM Promotion v WHERE v.status = :status")
    Double getTotalValueOfActiveVouchers(@Param("status") Promotion_Status status);

    // Find top discount vouchers
    @Query("SELECT v FROM Promotion v WHERE v.status = :status ORDER BY v.discount DESC")
    List<Promotion> findTopDiscountVouchers(@Param("status") Promotion_Status status);

    // Count vouchers created in date range
    @Query("SELECT COUNT(v) FROM Promotion v WHERE v.startTime >= :startDate AND v.startTime <= :endDate")
    long countVouchersCreatedInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}