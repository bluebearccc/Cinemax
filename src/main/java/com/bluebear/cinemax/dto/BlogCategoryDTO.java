package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryDTO {
    private Integer categoryID;
    private String categoryName;
}
