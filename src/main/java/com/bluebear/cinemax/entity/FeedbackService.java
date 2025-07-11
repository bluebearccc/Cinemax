package com.bluebear.cinemax.entity;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID")
    private Customer customer;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "Content", columnDefinition = "nvarchar(max)")
    private String content;

    @Column(name = "TheaterID", nullable = false)
    private Integer theaterId;

    @Column(name = "ServiceRate", nullable = false)
    private Integer serviceRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private FeedbackStatus status;

}
