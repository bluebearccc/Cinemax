package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.BlogSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogSectionRepository extends JpaRepository<BlogSection, Integer> {
}
