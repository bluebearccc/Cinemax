package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "ServiceFeedback")
public class FeedbackService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "CustomerID", nullable = false)
    private Integer customerId;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Content", columnDefinition = "nvarchar(max)")
    private String content;

    @Column(name = "TheaterID", nullable = false)
    private Integer theaterId;

    @Column(name = "ServiceRate", nullable = false)
    private Integer serviceRate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;
}
