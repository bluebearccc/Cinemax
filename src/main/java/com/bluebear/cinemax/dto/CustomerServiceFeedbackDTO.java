package com.bluebear.cinemax.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerServiceFeedbackDTO {
    private Integer feedbackId;
    private String fullName;
    private Integer customerId;
    private String email;
    private String phone;
    private String content;
    private String status;
    private Integer serviceRate;
    private Integer theaterId;

}
