package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.entity.TheaterStock;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookingService {



    List<TheaterStockDTO> getAvailableCombos();

    InvoiceDTO bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos);


    BookingPreviewDTO prepareBookingPreview(Integer scheduleId, Integer roomId,
                                            List<Integer> seatIds, String promotionCode,
                                            Map<Integer, Integer> comboQuantities);

    Map<Integer, Integer> extractComboQuantities(Map<String, String> allParams);



    double calculateTotalAmount(Integer scheduleId, List<Integer> seatIds, String promotionCode);



    List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks);
    List<TheaterStockDTO> filterCombosByKeyword(String keyword);
    void saveTransactionFromWebhook(SepayWebhookDTO payload);
    void finalizeBooking(Integer invoiceId);
}
