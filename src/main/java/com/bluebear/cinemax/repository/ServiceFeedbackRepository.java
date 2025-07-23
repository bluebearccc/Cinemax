package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.ServiceFeedback;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceFeedbackRepository extends JpaRepository<ServiceFeedback, Integer> {
    // Tùy chỉnh nếu muốn kiểm tra trùng feedback
    List<ServiceFeedback> findByStatus(FeedbackStatus status);
    long countByStatus(FeedbackStatus status);
    List<ServiceFeedback> findAllById(Integer id);
    Page<ServiceFeedback> findByServiceRateBetweenOrderByServiceRateDesc(
            Integer minRate,
            Integer maxRate,
            Pageable pageable
    );

    boolean existsByCustomer_IdAndTheater_TheaterID(Integer customerId, Integer theaterId);


    long countByTheater_theaterID(Integer theaterTheaterID);
}
