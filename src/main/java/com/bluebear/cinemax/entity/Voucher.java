package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "Promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    // Constructor without ID for creation
    public Voucher(String promotionCode, Integer discount, LocalDateTime startTime,
                   LocalDateTime endTime, Integer quantity, String status) {
        this.promotionCode = promotionCode;
        this.discount = discount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.quantity = quantity;
        this.status = status;
    }

    // Helper methods for display - Updated date format
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    // Additional date format methods
    public String getFormattedStartDate() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getFormattedEndDate() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    public String getFormattedStartTimeOnly() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getFormattedEndTimeOnly() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getFormattedDiscount() {
        return discount != null ? discount + "%" : "0%";
    }

    // Updated logic for Available/Expired status
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return "Available".equals(status) &&
                startTime != null && endTime != null &&
                now.isAfter(startTime) && now.isBefore(endTime) &&
                quantity != null && quantity > 0;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        // Expired if status is "Expired" OR if end time has passed
        return "Expired".equals(status) ||
                (endTime != null && now.isAfter(endTime));
    }

    // Helper method to check if voucher is usable
    public boolean isUsable() {
        return isActive() && !isExpired();
    }
}