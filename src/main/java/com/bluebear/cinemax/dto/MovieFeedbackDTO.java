package com.bluebear.cinemax.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFeedbackDTO {

    private Integer id;

    private Integer customerId;

    private String customerName;

    private Integer movieId;

    private String content;

    private Integer movieRate;

    private Integer totalComments;

    private LocalDateTime createdDate;

    private List<MovieFeedbackCommentDTO> comments;
}
