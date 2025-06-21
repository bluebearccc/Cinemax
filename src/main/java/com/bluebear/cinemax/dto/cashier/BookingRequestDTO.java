package com.bluebear.cinemax.dto.cashier;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class BookingRequestDTO {
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private Integer scheduleId;

    private List<Integer> selectedSeatIds;


    private Map<Integer, Integer> foodQuantities;

    private Integer employeeId;

    private Integer promotionId;

}