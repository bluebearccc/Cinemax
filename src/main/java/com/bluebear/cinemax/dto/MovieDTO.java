package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<String> genres;
    private List<String> actors;

    // Constructor without lists for basic movie creation
    public MovieDTO(Integer movieId, String movieName, String description, String image,
                    String banner, String studio, Integer duration, String trailer,
                    BigDecimal movieRate, LocalDate startDate, LocalDate endDate, String status) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.description = description;
        this.image = image;
        this.banner = banner;
        this.studio = studio;
        this.duration = duration;
        this.trailer = trailer;
        this.movieRate = movieRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Helper methods
    public String getFormattedDuration() {
        if (duration == null) return "N/A";
        int hours = duration / 60;
        int minutes = duration % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public String getFormattedRating() {
        if (movieRate == null) return "N/A";
        return movieRate + "/5";
    }

    public String getFormattedStartDate() {
        if (startDate == null) return "N/A";
        return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "N/A";
        return endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) return "";
        return String.join(", ", genres);
    }

    public String getActorsAsString() {
        if (actors == null || actors.isEmpty()) return "";
        return String.join(", ", actors);
    }

    // Status check methods
    public boolean isNowShowing() {
        LocalDate now = LocalDate.now();
        return "Active".equals(status) &&
                startDate != null &&
                endDate != null &&
                !startDate.isAfter(now) &&
                !endDate.isBefore(now);
    }

    public boolean isUpcoming() {
        LocalDate now = LocalDate.now();
        return "Active".equals(status) &&
                startDate != null &&
                startDate.isAfter(now);
    }

    public boolean isExpired() {
        LocalDate now = LocalDate.now();
        return endDate != null && endDate.isBefore(now);
    }
}