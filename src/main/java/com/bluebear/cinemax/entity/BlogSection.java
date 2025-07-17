package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BlogSection")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SectionID")
    private Integer sectionID;

    @ManyToOne
    @JoinColumn(name = "BlogID")
    private Blog blog;

    @Column(name = "SectionTitle", length = 255)
    private String sectionTitle;

    @Column(name = "SectionContent", columnDefinition = "NVARCHAR(MAX)")
    private String sectionContent;

    @Column(name = "SectionOrder")
    private Integer sectionOrder;
}