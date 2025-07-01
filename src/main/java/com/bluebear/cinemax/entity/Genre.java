package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "Genre")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"movieGenres"}) // Avoid circular reference in toString
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GenreID")
    private Integer genreId;

    @Column(name = "GenreName", nullable = false)
    private String genreName;

    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MovieGenre> movieGenres;

    // Constructor without movieGenres for basic creation
    public Genre(String genreName) {
        this.genreName = genreName;
    }

    // Override equals and hashCode to use only genreId for entity comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre)) return false;

        Genre genre = (Genre) o;
        return genreId != null ? genreId.equals(genre.genreId) : genre.genreId == null;
    }

    @Override
    public int hashCode() {
        return genreId != null ? genreId.hashCode() : 0;
    }
}