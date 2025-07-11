package com.bluebear.cinemax.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogLikeDTO {

    private Long id;

    private Integer blogID;

    private Integer customerID;

    private LocalDateTime likedAt;
}