package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogSectionDTO {
    private Integer sectionID;
    private Integer blogID;
    private String sectionTitle;
    private String sectionContent;
    private Integer sectionOrder;
}