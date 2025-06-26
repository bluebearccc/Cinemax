package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.FeedbackService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackServiceRepository extends JpaRepository<FeedbackService, Integer> {
    // Tùy chỉnh nếu muốn kiểm tra trùng feedback
}
