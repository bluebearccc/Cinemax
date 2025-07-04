package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Blog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BlogID")
    private Integer blogID;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "Image", columnDefinition = "NVARCHAR(500)")
    private String image;

    @ManyToOne
    @JoinColumn(name = "AuthorID")
    private Employee author;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "Like_Count")
    private Integer likeCount;

    @Column(name = "View_Count")
    private Integer viewCount;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Blog_Category",
            joinColumns = @JoinColumn(name = "BlogID"),
            inverseJoinColumns = @JoinColumn(name = "CategoryID")
    )
    private List<BlogCategory> categories;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
    private List<BlogSection> sections;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL)
    private List<BlogLike> blogLikes;
}
