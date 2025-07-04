package com.bluebear.cinemax.service.blog;

import com.bluebear.cinemax.dto.BlogDTO;
import com.bluebear.cinemax.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {
    BlogDTO toDTO(com.bluebear.cinemax.entity.Blog blog, boolean includeSections);

    Blog toEntity(BlogDTO dto, boolean includeSections);

    Page<BlogDTO> getAllBlogs(Pageable pageable);

    List<BlogDTO> getBlogsWithSameCate(BlogDTO blogDTO);

    BlogDTO findTop1Blog();

    Page<BlogDTO> findBlogsFilter(Integer categoryId, String title, Pageable pageable);

    BlogDTO getBlogById(Integer id);

    BlogDTO createBlog(BlogDTO dto);

    BlogDTO updateBlog(Integer id, BlogDTO dto);

    void deleteBlog(Integer id);
}




