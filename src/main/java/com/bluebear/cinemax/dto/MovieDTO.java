package com.bluebear.cinemax.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    // Constructors
    public MovieDTO() {}

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

    // Getters and Setters
    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public BigDecimal getMovieRate() {
        return movieRate;
    }

    public void setMovieRate(BigDecimal movieRate) {
        this.movieRate = movieRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
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

    // Kiểm tra trạng thái phim
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

    @Override
    public String toString() {
        return "MovieDTO{" +
                "movieId=" + movieId +
                ", movieName='" + movieName + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + duration +
                ", movieRate=" + movieRate +
                '}';
    }
}