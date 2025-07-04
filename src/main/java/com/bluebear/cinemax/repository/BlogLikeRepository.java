package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Integer> {

    Optional<BlogLike> findByBlog_BlogIDAndCustomer_Id(Integer blogId, Integer customerId);

    boolean existsByBlog_BlogIDAndCustomer_Id(Integer blogId, Integer customerId);

    void deleteByBlog_BlogIDAndCustomer_Id(Integer blogId, Integer customerId);
}