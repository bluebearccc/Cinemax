package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "MovieFeedbackComment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFeedbackComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CommentID")
    private Integer commentId;

    @ManyToOne()
    @JoinColumn(name = "FeedbackID", nullable = false)
    private MovieFeedback feedback;

    @ManyToOne()
    @JoinColumn(name = "AuthorCustomerID", nullable = false)
    private Customer author;

    @ManyToOne()
    @JoinColumn(name = "RepliedToCustomerID")
    private Customer repliedTo;

    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "CreatedDate", columnDefinition = "DATETIME DEFAULT GETDATE()")
    private LocalDateTime createdDate;

}
