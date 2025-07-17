package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.TheaterStock;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookingSerivceSF {
//    void finalizeBooking(Integer invoiceId);

    Optional<PromotionDTO> validatePromotionCode(String code);

    List<TheaterStockDTO> getAvailableCombos();

    InvoiceDTO bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos);

    Map<String, Object> checkPromotionCode(String code, double totalAmount);

    BookingPreviewDTO prepareBookingPreview(Integer scheduleId, Integer roomId,
                                            List<Integer> seatIds, String promotionCode,
                                            Map<Integer, Integer> comboQuantities);

    Map<Integer, Integer> extractComboQuantities(Map<String, String> allParams);

    Map<String, Object> applyPromotionCode(String code, double totalAmount);

    double calculateTotalAmount(Integer scheduleId, List<Integer> seatIds, String promotionCode);

    List<SeatDTO> toSeatDTOList(List<Seat> seats);

    List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks);
}
