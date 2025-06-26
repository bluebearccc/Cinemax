package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "Promotion")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromotionID")
    private Integer promotionId;

    @Column(name = "PromotionCode", length = 10, unique = true, nullable = false)
    private String promotionCode;

    @Column(name = "Discount", nullable = false)
    private Integer discount;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "Status", length = 20, nullable = false)
    private String status;

    // Constructors
    public Voucher() {}

    public Voucher(String promotionCode, Integer discount, LocalDateTime startTime,
                   LocalDateTime endTime, Integer quantity, String status) {
        this.promotionCode = promotionCode;
        this.discount = discount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.quantity = quantity;
        this.status = status;
    }

    // Getters and Setters
    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper methods for display
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "";
    }

    public String getFormattedDiscount() {
        return discount != null ? discount + "%" : "0%";
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "Active".equals(status) &&
                startTime != null && endTime != null &&
                now.isAfter(startTime) && now.isBefore(endTime) &&
                quantity != null && quantity > 0;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return endTime != null && now.isAfter(endTime);
    }
}