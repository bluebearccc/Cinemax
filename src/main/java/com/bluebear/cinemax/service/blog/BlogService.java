package com.bluebear.cinemax.service.blog;

import com.bluebear.cinemax.dto.BlogDTO;
import com.bluebear.cinemax.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BlogService {
    BlogDTO toDTO(com.bluebear.cinemax.entity.Blog blog, boolean includeSections);

    Blog toEntity(BlogDTO dto, boolean includeSections);

    Page<BlogDTO> getAllBlogs(Pageable pageable);

    List<BlogDTO> getBlogsWithSameCate(BlogDTO blogDTO);

    BlogDTO findTop1Blog();

    Page<BlogDTO> findBlogsFilter(Integer categoryId, String title, Pageable pageable);

    BlogDTO getBlogById(Integer id);

    BlogDTO createBlog(BlogDTO dto, MultipartFile img);

    BlogDTO updateBlog(Integer id, BlogDTO dto, MultipartFile img);

    BlogDTO updateBlog(Integer id, BlogDTO dto);

    void deleteBlog(Integer id);

    long getTotalBlogCount();

    long getTotalViewCount();

    public String saveImage(MultipartFile img) throws IOException;
}




