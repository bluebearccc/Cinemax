package com.bluebear.cinemax.service.blogsection;

import com.bluebear.cinemax.dto.BlogSectionDTO;
import com.bluebear.cinemax.entity.Blog;
import com.bluebear.cinemax.entity.BlogSection;
import com.bluebear.cinemax.repository.BlogRepository;
import com.bluebear.cinemax.repository.BlogSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogSectionServiceImpl implements BlogSectionService {

    @Autowired
    private BlogSectionRepository blogSectionRepository;
    @Autowired
    private BlogRepository blogRepository;

    public BlogSectionDTO toDTO(BlogSection section) {
        return BlogSectionDTO.builder()
                .sectionID(section.getSectionID())
                .blogID(section.getBlog().getBlogID())
                .sectionTitle(section.getSectionTitle())
                .sectionContent(section.getSectionContent())
                .sectionOrder(section.getSectionOrder())
                .build();
    }

    public BlogSection toEntity(BlogSectionDTO dto) {
        Blog blog = blogRepository.findById(dto.getBlogID()).orElse(null);

        return BlogSection.builder()
                .sectionID(dto.getSectionID())
                .blog(blog)
                .sectionTitle(dto.getSectionTitle())
                .sectionContent(dto.getSectionContent())
                .sectionOrder(dto.getSectionOrder())
                .build();
    }

    public List<BlogSectionDTO> getAllSections() {
        return blogSectionRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BlogSectionDTO getSectionById(Integer id) {
        BlogSection section = blogSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found with ID: " + id));
        return toDTO(section);
    }

    public BlogSection findById(Integer id) {
        return blogSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found with ID: " + id));
    }

    public BlogSectionDTO createSection(BlogSectionDTO dto) {
        BlogSection saved = blogSectionRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    public BlogSectionDTO updateSection(Integer id, BlogSectionDTO dto) {
        BlogSection existing = blogSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Section not found with ID: " + id));

        existing.setSectionTitle(dto.getSectionTitle());
        existing.setSectionContent(dto.getSectionContent());
        existing.setSectionOrder(dto.getSectionOrder());

        Blog blog = blogRepository.findById(dto.getBlogID()).orElse(null);
        existing.setBlog(blog);

        BlogSection updated = blogSectionRepository.save(existing);
        return toDTO(updated);
    }

    public void deleteSection(Integer id) {
        if (!blogSectionRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Section not found with ID: " + id);
        }
        blogSectionRepository.deleteById(id);
    }
}

