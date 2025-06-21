package com.bluebear.cinemax.dto.cashier;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BookingResponseDTO {

    private Integer invoiceId;
    private String bookingDate;
    private Double totalPrice;


    private String customerName;
    private String customerPhone;


    private String movieName;
    private int movieDuration;
    private String scheduleTime;
    private String roomName;

    private List<String> seatPositions;


    private List<FoodItemDetail> foodItems;
    private Double totalFoodPrice;


    private Double totalTicketPrice;


    @Data
    @Builder
    public static class FoodItemDetail {
        private String name;
        private int quantity;
        private Double unitPrice;
    }
}