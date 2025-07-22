package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.FeedbackService;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackServiceRepository extends JpaRepository<FeedbackService, Integer> {
    // Tùy chỉnh nếu muốn kiểm tra trùng feedback
    List<FeedbackService> findByStatus(FeedbackStatus status);
    long countByStatus(FeedbackStatus status);
    List<FeedbackService> findAllById(Integer id);
    Page<FeedbackService> findByServiceRateBetweenOrderByServiceRateDesc(
            Integer minRate,
            Integer maxRate,
            Pageable pageable
    );
    boolean existsByCustomer_IdAndTheaterId(Integer customerId, Integer theaterId);
    // Tùy chỉnh nếu muốn kiểm tra trùng feedback
}
