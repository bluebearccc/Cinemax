package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingPreviewDTO {
    private ScheduleDTO schedule;
    private RoomDTO room;
    private List<SeatDTO> selectedSeats;
    private List<TheaterStockDTO> combos;
    private Map<Integer, Integer> comboQuantities;
    private double totalPrice;
    private double finalPrice;
    private PromotionDTO promotion;


}
