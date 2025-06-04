package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.ServiceFeedbackStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFeedbackDTO {
    private Integer id;
    private Integer customerId;
    private String content;
    private ServiceFeedbackStatus status;
    private CustomerDTO customer;
}
