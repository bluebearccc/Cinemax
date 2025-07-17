package com.bluebear.cinemax.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResultDTO {
    private Integer invoiceId;
    private LocalDateTime bookingDate;
    private String customerName;
    private String customerPhone;
    private Double totalPrice;
    private Double totalTicketPrice;
    private Double totalFoodPrice;
    private String movieName;
    private String movieDuration;
    private String scheduleTime;
    private String roomName;
    private List<String> seatPositions;
    private List<FoodItemDTO> foodItems;
    private Double subTotalPrice;
    private String promotionName;
    private Double discountAmount;
    private Double unitTicketPrice;

    @Data
    @Builder
    public static class FoodItemDTO {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
    }
}