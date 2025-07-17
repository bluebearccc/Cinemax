package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.ServiceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceFeedbackRepository extends JpaRepository<ServiceFeedback, Integer> {
    long countByTheater_theaterID(Integer theaterTheaterID);
}

