package com.bluebear.cinemax.dto.cashier;

import com.bluebear.cinemax.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Integer movieId;
    private String movieName;
    private String description;
    private String image;
    private String banner;
    private String studio;
    private Integer duration;
    private String trailer;
    private BigDecimal movieRate;
    private String actor;
    private LocalDate startDate;
    private LocalDate endDate;
    private MovieStatus status;
    private List<Integer> genreIds;        // Chỉ truyền ID hoặc tên genre
    private List<Integer> scheduleIds;     // Tùy frontend cần gì
    private List<Integer> feedbackIds;     // Hoặc có thể tạo thêm DTO khác

    public void setStatus(Movie.MovieStatus status) {
        this.status = MovieStatus.valueOf(status.name());
    }

    public enum MovieStatus {
        Active, Removed
    }
}
