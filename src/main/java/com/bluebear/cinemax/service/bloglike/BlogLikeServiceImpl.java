package com.bluebear.cinemax.service.bloglike;

import com.bluebear.cinemax.dto.BlogLikeDTO;
import com.bluebear.cinemax.entity.Blog;
import com.bluebear.cinemax.entity.BlogLike;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.BlogLikeRepository;
import com.bluebear.cinemax.repository.BlogRepository;
import com.bluebear.cinemax.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlogLikeServiceImpl implements BlogLikeService {

    private final BlogLikeRepository blogLikeRepository;
    private final BlogRepository blogRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public BlogLikeDTO likeBlog(BlogLikeDTO dto) {
        if (hasLiked(dto.getBlogID(), dto.getCustomerID())) {
            throw new RuntimeException("User already liked this blog");
        }

        BlogLike blogLike = toEntity(dto);
        blogLike.setLikedAt(LocalDateTime.now());

        return toDTO(blogLikeRepository.save(blogLike));
    }

    @Override
    @Transactional
    public void unlikeBlog(Integer blogId, Integer customerId) {
        if (!hasLiked(blogId, customerId)) {
            throw new RuntimeException("User hasn't liked this blog");
        }
        blogLikeRepository.deleteByBlog_BlogIDAndCustomer_Id(blogId, customerId);
    }

    @Override
    public boolean hasLiked(Integer blogId, Integer customerId) {
        return blogLikeRepository.existsByBlog_BlogIDAndCustomer_Id(blogId, customerId);
    }

    @Override
    public BlogLikeDTO toDTO(BlogLike blogLike) {
        return BlogLikeDTO.builder()
                .id(blogLike.getId())
                .blogID(blogLike.getBlog().getBlogID())
                .customerID(blogLike.getCustomer().getId())
                .likedAt(blogLike.getLikedAt())
                .build();
    }

    @Override
    public BlogLike toEntity(BlogLikeDTO dto) {
        Blog blog = blogRepository.findById(dto.getBlogID())
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        Customer customer = customerRepository.findById(dto.getCustomerID())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return BlogLike.builder()
                .id(dto.getId())
                .blog(blog)
                .customer(customer)
                .likedAt(dto.getLikedAt())
                .build();
    }
}

