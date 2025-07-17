package com.bluebear.cinemax.service.blogcategory;

import com.bluebear.cinemax.dto.BlogCategoryDTO;
import com.bluebear.cinemax.entity.BlogCategory;

import java.util.List;

public interface BlogCategoryService {
    BlogCategoryDTO toDTO(BlogCategory category);

    BlogCategory toEntity(BlogCategoryDTO dto);

    List<BlogCategoryDTO> getAllCategories();

    BlogCategoryDTO getCategoryById(Integer id);

    BlogCategoryDTO createCategory(BlogCategoryDTO dto);

    BlogCategoryDTO updateCategory(Integer id, BlogCategoryDTO dto);

    BlogCategoryDTO findCategoryByName(String categoryName);

    void deleteCategory(Integer id);
}
