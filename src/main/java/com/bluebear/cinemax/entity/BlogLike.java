package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Blog_Like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"BlogID", "CustomerID"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BlogID", nullable = false, foreignKey = @ForeignKey(name = "FK_BlogLike_Blog"))
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", nullable = false, foreignKey = @ForeignKey(name = "FK_BlogLike_Customer"))
    private Customer customer;

    @Column(name = "LikedAt")
    private LocalDateTime likedAt;
}

