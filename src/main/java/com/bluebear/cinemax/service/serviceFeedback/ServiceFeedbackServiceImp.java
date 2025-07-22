package com.bluebear.cinemax.service.serviceFeedback;

import com.bluebear.cinemax.dto.ServiceFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.FeedbackService;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class ServiceFeedbackServiceImp implements ServiceFeedbackService {
    public ServiceFeedbackDTO toDTO(FeedbackService feedback) {
        ServiceFeedbackDTO dto = new ServiceFeedbackDTO();
        dto.setCustomerId(feedback.getCustomer().getId());
        dto.setTheaterId(feedback.getTheaterId());
        dto.setContent(feedback.getContent());
        dto.setServiceRate(feedback.getServiceRate());
        dto.setCreatedDate(feedback.getCreatedDate());
        dto.setStatus(feedback.getStatus());
        return dto;
    }

    public FeedbackService toEntity(ServiceFeedbackDTO dto, Customer customer) {
        FeedbackService feedback = new FeedbackService();
        feedback.setCustomer(customer);
        feedback.setContent(dto.getContent());
        feedback.setServiceRate(dto.getServiceRate());
        feedback.setCreatedDate(LocalDateTime.now());
        feedback.setTheaterId(dto.getTheaterId());
        feedback.setStatus(dto.getServiceRate() < 4 ? FeedbackStatus.Not_Suported : FeedbackStatus.Suported);
        return feedback;
    }
}
