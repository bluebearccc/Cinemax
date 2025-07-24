package com.bluebear.cinemax.service.bloglike;

import com.bluebear.cinemax.dto.BlogLikeDTO;
import com.bluebear.cinemax.entity.BlogLike;

public interface BlogLikeService {

    BlogLikeDTO likeBlog(BlogLikeDTO dto);

    void unlikeBlog(Integer blogId, Integer customerId);

    boolean hasLiked(Integer blogId, Integer customerId);

    BlogLikeDTO toDTO(com.bluebear.cinemax.entity.BlogLike blogLike);

    BlogLike toEntity(BlogLikeDTO dto);

    long getTotalBlogLikeCount();
}
