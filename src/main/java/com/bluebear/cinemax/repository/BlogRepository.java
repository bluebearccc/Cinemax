package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Blog;
import com.bluebear.cinemax.entity.BlogCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {
    Page<Blog> findAll(Pageable pageable);

    Page<Blog> findByTitleContaining(String title, Pageable pageable);

    @Query("SELECT b FROM Blog b JOIN b.categories c WHERE c.categoryID = :categoryId")
    Page<Blog> findByCategoryId(Integer categoryId, Pageable pageable);

    @Query("""
    SELECT b FROM Blog b
    JOIN b.categories c
    WHERE c.categoryID = :categoryId
      AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))
    """)
    Page<Blog> findByCategoryIdAndTitleContainingIgnoreCase(Integer categoryId, String title, Pageable pageable);

    @Query("SELECT b FROM Blog b JOIN b.categories c WHERE c IN :categories AND b.blogID <> :blogId")
    List<Blog> findRelatedBlogs(List<BlogCategory> categories, Integer blogId, Pageable pageable);

    Blog findTopByOrderByViewCountDescLikeCountDesc();

}
