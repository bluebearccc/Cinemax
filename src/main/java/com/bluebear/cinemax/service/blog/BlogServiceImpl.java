package com.bluebear.cinemax.service.blog;

import com.bluebear.cinemax.dto.BlogDTO;
import com.bluebear.cinemax.dto.BlogSectionDTO;
import com.bluebear.cinemax.entity.Blog;
import com.bluebear.cinemax.entity.BlogCategory;
import com.bluebear.cinemax.entity.BlogSection;
import com.bluebear.cinemax.repository.BlogCategoryRepository;
import com.bluebear.cinemax.repository.BlogRepository;
import com.bluebear.cinemax.repository.EmployeeRepository;
import com.bluebear.cinemax.service.blogcategory.BlogCategoryService;
import com.bluebear.cinemax.service.blogsection.BlogSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BlogCategoryService blogCategoryService;
    @Autowired
    private BlogSectionService blogSectionService;
    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    public BlogDTO toDTO(Blog blog, boolean includeSections) {
        BlogDTO.BlogDTOBuilder builder = BlogDTO.builder()
                .blogID(blog.getBlogID())
                .title(blog.getTitle())
                .content(blog.getContent())
                .image(blog.getImage())
                .authorID(blog.getAuthor().getId())
                .authorName(blog.getAuthor().getFullName())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .likeCount(blog.getLikeCount())
                .viewCount(blog.getViewCount())
                .categories(blog.getCategories().stream()
                        .map(blogCategoryService::toDTO)
                        .collect(Collectors.toList())
                );

        if (includeSections && blog.getSections() != null) {
            builder.sections(
                    blog.getSections().stream()
                            .map(blogSectionService::toDTO)
                            .collect(Collectors.toList())
            );
        }

        return builder.build();
    }

    public Blog toEntity(BlogDTO dto, boolean includeSections) {
        Blog blog = Blog.builder()
                .blogID(dto.getBlogID())
                .title(dto.getTitle())
                .content(dto.getContent())
                .image(dto.getImage())
                .author(employeeRepository.findById(dto.getAuthorID()).orElse(null))
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .likeCount(dto.getLikeCount())
                .viewCount(dto.getViewCount())
                .categories(dto.getCategories().stream()
                        .map(catDTO -> blogCategoryRepository.findById(catDTO.getCategoryID()).orElse(null))
                        .collect(Collectors.toList())
                )
                .build();

        if (includeSections && dto.getSections() != null) {
            List<BlogSection> sections = dto.getSections().stream()
                    .map(sectionDTO -> blogSectionService.toEntity(sectionDTO))
                    .collect(Collectors.toList());
            blog.setSections(sections);
        }

        return blog;
    }

    public Page<BlogDTO> getAllBlogs(Pageable pageable) {
        return blogRepository.findAll(pageable).map(blog -> toDTO(blog, false));
    }

    public List<BlogDTO> getBlogsWithSameCate(BlogDTO blogDTO) {
        List<BlogCategory> categories = blogDTO.getCategories().stream().map(catDTO -> blogCategoryService.toEntity(catDTO)).toList();
        return blogRepository.findRelatedBlogs(categories, blogDTO.getBlogID(), PageRequest.of(0, 5)).stream().map(blog -> toDTO(blog, false)).collect(Collectors.toList());
    }

    public BlogDTO findTop1Blog() {
        return toDTO(blogRepository.findTopByOrderByViewCountDescLikeCountDesc(), true);
    }

    public Page<BlogDTO> findBlogsFilter(Integer categoryId, String title, Pageable pageable) {

        if (categoryId != null && title != null) {
            return blogRepository.findByCategoryIdAndTitleContainingIgnoreCase(categoryId, title, pageable).map(blog -> toDTO(blog, false));
        } else if (categoryId != null) {
            return blogRepository.findByCategoryId(categoryId, pageable).map(blog -> toDTO(blog, false));
        } else {
            return blogRepository.findByTitleContaining(title, pageable).map(blog -> toDTO(blog, false));
        }
    }

    public BlogDTO getBlogById(Integer id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with ID: " + id));
        return toDTO(blog, true);
    }

    public BlogDTO createBlog(BlogDTO dto, MultipartFile img) {
        Blog blog = toEntity(dto, false);
        Blog saved = blogRepository.save(blog);

        for (BlogSectionDTO sectionDTO : dto.getSections()) {
            sectionDTO.setBlogID(saved.getBlogID());
        }

        List<BlogSection> sections = dto.getSections().stream()
                .map(sectionDTO -> blogSectionService.toEntity(sectionDTO))
                .collect(Collectors.toList());
        saved.setSections(sections);
        BlogDTO newBlog = toDTO(saved, true);
        updateBlog(newBlog.getBlogID(), newBlog, img);
        return toDTO(saved, true);
    }

    @Override
    public BlogDTO updateBlog(Integer id, BlogDTO dto, MultipartFile img) {
            String imageUrl = null;
            try {
                imageUrl = "/uploads/blog_images/" + saveImage(img);
            } catch (IOException e) {
                System.out.println("Error saving image: " + e.getMessage());
            }

        Blog existing = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot update. Blog not found with ID: " + id));

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setUpdatedAt(dto.getUpdatedAt());
        existing.setLikeCount(dto.getLikeCount());
        existing.setImage(imageUrl);
        existing.setViewCount(dto.getViewCount());
        existing.setCategories(dto.getCategories().stream()
                .map(catDTO -> blogCategoryRepository.findById(catDTO.getCategoryID()).orElse(null))
                .collect(Collectors.toList())
        );

        if (dto.getSections() != null) {
            List<BlogSection> sections = dto.getSections().stream()
                    .map(sectionDTO -> BlogSection.builder()
                            .sectionID(sectionDTO.getSectionID())
                            .sectionTitle(sectionDTO.getSectionTitle())
                            .sectionContent(sectionDTO.getSectionContent())
                            .sectionOrder(sectionDTO.getSectionOrder())
                            .blog(existing)
                            .build())
                    .collect(Collectors.toList());
            existing.getSections().clear();
            existing.getSections().addAll(sections);
        }

        Blog updated = blogRepository.save(existing);
        return toDTO(updated, true);
    }

    public BlogDTO updateBlog(Integer id, BlogDTO dto) {
        Blog existing = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cannot update. Blog not found with ID: " + id));

        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setUpdatedAt(dto.getUpdatedAt());
        existing.setLikeCount(dto.getLikeCount());
        existing.setImage(dto.getImage());
        existing.setViewCount(dto.getViewCount());
        existing.setCategories(dto.getCategories().stream()
                .map(catDTO -> blogCategoryRepository.findById(catDTO.getCategoryID()).orElse(null))
                .collect(Collectors.toList())
        );

        if (dto.getSections() != null) {
            List<BlogSection> sections = dto.getSections().stream()
                    .map(sectionDTO -> BlogSection.builder()
                            .sectionID(sectionDTO.getSectionID())
                            .sectionTitle(sectionDTO.getSectionTitle())
                            .sectionContent(sectionDTO.getSectionContent())
                            .sectionOrder(sectionDTO.getSectionOrder())
                            .blog(existing)
                            .build())
                    .collect(Collectors.toList());
            existing.getSections().clear();
            existing.getSections().addAll(sections);
        }

        Blog updated = blogRepository.save(existing);
        return toDTO(updated, true);
    }

    public void deleteBlog(Integer id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete. Blog not found with ID: " + id);
        }
        blogRepository.deleteById(id);
    }

    @Override
    public long getTotalBlogCount() {
        return blogRepository.countAllBlogs();
    }

    @Override
    public long getTotalViewCount() {
        return blogRepository.getTotalViewCount();
    }

    @Override
    public String saveImage(MultipartFile img) throws IOException {
        String uploadDir = "uploads/blog_images";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = img.getOriginalFilename();
        filename = filename.replaceAll("\\s+", "");
        filename = filename.replaceAll("[\\p{Punct}&&[^.-]]", "");
        Path filePath = uploadPath.resolve(filename);
//        if (Files.exists(filePath)) {
//            int counter = 1;
//            String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
//            String extension = filename.substring(filename.lastIndexOf('.'));
//            while (Files.exists(filePath)) {
//                filename = nameWithoutExtension + "(" + counter + ")" + extension;
//                filePath = uploadPath.resolve(filename);
//                counter++;
//            }
//        }
        Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}

