package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MovieFeedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "CustomerID", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "MovieID", nullable = false)
    private Movie movie;

    @Column(name = "Content")
    private String content;

    @Column(name = "MovieRate")
    private Integer movieRate = 5;
}