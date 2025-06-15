package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "MovieFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne()
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @ManyToOne()
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    @Column(name = "Content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "MovieRate")
    private Integer movieRate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private Date createdDate;
}
