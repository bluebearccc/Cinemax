//package com.bluebear.cinemax.entity;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.List;
//import java.util.Objects;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "Movie_Genre", schema = "dbo")
//public class MovieGenre {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "GenreID")
//    private Integer genreID;
//
//    @Column(name = "GenreName", nullable = false, length = 255)
//    private String genreName;
//
//    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Movie> movies;
//}

