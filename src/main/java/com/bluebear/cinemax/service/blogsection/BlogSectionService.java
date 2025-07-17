package com.bluebear.cinemax.service.blogsection;

import com.bluebear.cinemax.dto.BlogSectionDTO;
import com.bluebear.cinemax.entity.BlogSection;

import java.util.List;

public interface BlogSectionService {
    BlogSectionDTO toDTO(BlogSection section);

    BlogSection toEntity(BlogSectionDTO dto);

    List<BlogSectionDTO> getAllSections();

    BlogSectionDTO getSectionById(Integer id);

    BlogSection findById(Integer id);

    BlogSectionDTO createSection(BlogSectionDTO dto);

    BlogSectionDTO updateSection(Integer id, BlogSectionDTO dto);

    void deleteSection(Integer id);
}
