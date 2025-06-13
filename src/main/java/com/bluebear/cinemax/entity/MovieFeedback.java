package com.bluebear.cinemax.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Objects;
@Setter
@Getter
@Entity
@Table(name = "MovieFeedback")
public class MovieFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // Tên trường "CustomerID" và "MovieID" viết hoa chữ cái đầu không theo quy ước Java (nên là customerID, movieID)
    @Column(name = "CustomerID", nullable = false, length = 255)
    private String customerID;

    @Column(name = "MovieID", nullable = false, length = 255)
    private String movieID;

    @Column(name = "Content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "MovieRate")
    private Integer movieRate;



    // Constructor mặc định (bắt buộc bởi JPA)
    public MovieFeedback() {
    }

    public MovieFeedback(Long id, String customerID, String movieID, String content, Integer movieRate) {
        this.id = id;
        this.customerID = customerID;
        this.movieID = movieID;
        this.content = content;
        this.movieRate = movieRate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MovieFeedback that = (MovieFeedback) o;
        return Objects.equals(id, that.id) && Objects.equals(customerID, that.customerID) && Objects.equals(movieID, that.movieID) && Objects.equals(content, that.content) && Objects.equals(movieRate, that.movieRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerID, movieID, content, movieRate);
    }

    @Override
    public String toString() {
        return "MovieFeedback{" +
                "id=" + id +
                ", CustomerID='" + customerID + '\'' +
                ", MovieID='" + movieID + '\'' +
                ", content='" + content + '\'' +
                ", movieRate=" + movieRate +
                '}';
    }
}


    /*
    public MovieFeedback(Long id, String customerID, String movieID, String contentValue, Integer movieRate, Integer foodRate, Integer serviceRate) {
        this.id = id;
        this.CustomerID = customerID;
        this.MovieID = movieID;
        this.content = contentValue; // Sửa lỗi từ "content = content;" và dùng tham số rõ ràng
        this.movieRate = movieRate;
        this.foodRate = foodRate;
        this.serviceRate = serviceRate;
    }
    */




