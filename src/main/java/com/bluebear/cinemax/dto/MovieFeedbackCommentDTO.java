package com.bluebear.cinemax.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFeedbackCommentDTO {

    private Integer commentId;

    private Integer feedbackId;

    private Integer authorCustomerId;

    private Integer repliedToCustomerId;

    private String content;

    private LocalDateTime createdDate;

    private String authorName;

    private String repliedToName;

}
