package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.ServiceFeedback_Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceFeedbackDTO {

    private Integer id;

    private Integer customerId;

    private LocalDateTime createdDate;

    private String content;

    private Integer theaterId;

    private Integer serviceRate;

    private ServiceFeedback_Status status;
}

