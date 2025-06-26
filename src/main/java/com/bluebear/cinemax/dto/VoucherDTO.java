package com.bluebear.cinemax.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VoucherDTO {

    private Integer promotionId;
    private String promotionCode;
    private Integer discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private String status;

    // Constructors
    public VoucherDTO() {}

    public VoucherDTO(Integer promotionId, String promotionCode, Integer discount,
                      LocalDateTime startTime, LocalDateTime endTime, Integer quantity, String status) {
        this.promotionId = promotionId;
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

    // Helper methods for validation and display
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
    }

    public boolean isValid() {
        return promotionCode != null && !promotionCode.trim().isEmpty() &&
                discount != null && discount >= 0 && discount <= 100 &&
                startTime != null && endTime != null && startTime.isBefore(endTime) &&
                quantity != null && quantity >= 0 &&
                status != null && !status.trim().isEmpty();
    }
}