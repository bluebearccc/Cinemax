package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieFeedbackDTO {
    private Integer id;
    private String customerName;
    private String content;
    private Integer movieRate;
}
