package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    private Integer promotionId;
    private String promotionCode;
    private Integer discount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer quantity;
    private String status;

    // Helper methods for validation and display - Updated date format
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    // For HTML datetime-local input (form fields)
    public String getFormattedStartTimeForInput() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
    }

    public String getFormattedEndTimeForInput() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "";
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

    // Updated validation for create mode (all fields required)
    public boolean isValid() {
        return promotionCode != null && !promotionCode.trim().isEmpty() &&
                discount != null && discount >= 0 && discount <= 100 &&
                startTime != null && endTime != null && startTime.isBefore(endTime) &&
                quantity != null && quantity >= 0 &&
                status != null && !status.trim().isEmpty() &&
                isValidStatus(status);
    }

    // Validation for edit mode (some fields can be null)
    public boolean isValidForUpdate() {
        // If promotion code is provided, it must be valid
        if (promotionCode != null && promotionCode.trim().isEmpty()) {
            return false;
        }

        // If discount is provided, it must be between 0-100
        if (discount != null && (discount < 0 || discount > 100)) {
            return false;
        }

        // If both start and end time are provided, start must be before end
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            return false;
        }

        // If quantity is provided, it must be >= 0
        if (quantity != null && quantity < 0) {
            return false;
        }

        // If status is provided, it must be valid
        if (status != null && !status.trim().isEmpty() && !isValidStatus(status)) {
            return false;
        }

        return true;
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return "Available".equals(status) || "Expired".equals(status);
    }
}