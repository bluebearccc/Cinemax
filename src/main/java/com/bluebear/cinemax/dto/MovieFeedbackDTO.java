package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieFeedbackDTO {
    private Integer id;
    private Integer customerId;
    private Integer movieId;
    private String content;
    private Integer movieRate;
    private CustomerDTO customer;
    private MovieDTO movie;
}