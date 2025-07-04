package com.bluebear.cinemax.service.blogcategory;

import com.bluebear.cinemax.dto.BlogCategoryDTO;
import com.bluebear.cinemax.entity.BlogCategory;
import com.bluebear.cinemax.repository.BlogCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogCategoryServiceImpl implements BlogCategoryService {

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    public BlogCategoryDTO toDTO(BlogCategory category) {
        return BlogCategoryDTO.builder()
                .categoryID(category.getCategoryID())
                .categoryName(category.getCategoryName())
                .build();
    }

    public BlogCategory toEntity(BlogCategoryDTO dto) {
        return BlogCategory.builder()
                .categoryID(dto.getCategoryID())
                .categoryName(dto.getCategoryName())
                .build();
    }

    public List<BlogCategoryDTO> getAllCategories() {
        return blogCategoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BlogCategoryDTO getCategoryById(Integer id) {
        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        return toDTO(category);
    }

    public BlogCategoryDTO createCategory(BlogCategoryDTO dto) {
        BlogCategory saved = blogCategoryRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    public BlogCategoryDTO updateCategory(Integer id, BlogCategoryDTO dto) {
        BlogCategory existing = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        existing.setCategoryName(dto.getCategoryName());
        BlogCategory updated = blogCategoryRepository.save(existing);
        return toDTO(updated);
    }

    public void deleteCategory(Integer id) {
        if (!blogCategoryRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Category not found with ID: " + id);
        }
        blogCategoryRepository.deleteById(id);
    }
}

