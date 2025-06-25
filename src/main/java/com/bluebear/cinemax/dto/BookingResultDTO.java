package com.bluebear.cinemax.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BookingResultDTO {
    private Integer invoiceId;
    private String bookingDate;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalPrice;
    private BigDecimal totalTicketPrice;
    private BigDecimal totalFoodPrice;
    private String movieName;
    private String movieDuration;
    private String scheduleTime;
    private String roomName;
    private List<String> seatPositions;
    private List<FoodItemDTO> foodItems;

    @Data
    @Builder
    public static class FoodItemDTO {
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
    }
}