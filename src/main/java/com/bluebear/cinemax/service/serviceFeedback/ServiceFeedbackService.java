package com.bluebear.cinemax.service.serviceFeedback;

import com.bluebear.cinemax.dto.ServiceFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.FeedbackService;

public interface ServiceFeedbackService {
    ServiceFeedbackDTO toDTO(FeedbackService feedback);
    FeedbackService toEntity(ServiceFeedbackDTO dto, Customer customer);
}
