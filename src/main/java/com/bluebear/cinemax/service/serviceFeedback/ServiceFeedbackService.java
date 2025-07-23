package com.bluebear.cinemax.service.serviceFeedback;

import com.bluebear.cinemax.dto.ServiceFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.ServiceFeedback;

import java.util.List;

public interface ServiceFeedbackService {
    ServiceFeedbackDTO toDTO(ServiceFeedback feedback);
    ServiceFeedback toEntity(ServiceFeedbackDTO dto, Customer customer);
    ServiceFeedback toEntity(ServiceFeedbackDTO dto);
    ServiceFeedbackDTO create(ServiceFeedbackDTO dto);
    ServiceFeedbackDTO update(Integer id, ServiceFeedbackDTO dto);
    boolean delete(Integer id);
    ServiceFeedbackDTO getById(Integer id);
    List<ServiceFeedbackDTO> getAll();

    int countByTheaterId(Integer theaterId);
}
