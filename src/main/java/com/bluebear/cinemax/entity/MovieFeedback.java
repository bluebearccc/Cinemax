package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MovieFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "MovieRate")
    private Integer movieRate;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovieFeedbackComment> comments;
}