package com.bluebear.cinemax.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class BookingRequestDTO {
    private Integer scheduleId;
    private List<Integer> selectedSeats;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer promotionId;
    private Map<Integer, Integer> foodQuantities;
    private String paymentMethod;
}