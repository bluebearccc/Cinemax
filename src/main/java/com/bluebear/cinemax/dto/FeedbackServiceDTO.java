package com.bluebear.cinemax.dto;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import lombok.*;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackServiceDTO {
    private Integer id;
    private Integer customerId;
    private LocalDateTime createdDate;
    private String content;
    private Integer theaterId;
    private Integer serviceRate;
    private FeedbackStatus status;

}
