package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.FeedbackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceFeedbackDTO {
    private Integer id;
    private Integer customerId;
    private LocalDateTime createdDate;
    private String content;
    private Integer theaterId;
    private Integer serviceRate;
    private FeedbackStatus status;
}
