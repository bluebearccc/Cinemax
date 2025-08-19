package com.bluebear.cinemax.service.bookingSF;

import com.bluebear.cinemax.dto.BookingPreviewDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.TheaterStock;

import java.util.List;
import java.util.Map;

public interface BookingServiceSF {
    Map<String, Object> getTicketTemplateData(Integer invoiceId);
    InvoiceDTO getInvoiceById(Integer invoiceId);

    BookingPreviewDTO reconstructBookingPreview(Integer invoiceId);

    InvoiceDTO createTemporaryInvoice(BookingPreviewDTO previewData, Integer customerId);

    void cancelInvoice(Integer invoiceId);


    List<TheaterStockDTO> getAvailableCombos(Integer roomId);

    InvoiceDTO bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos);


    BookingPreviewDTO prepareBookingPreview(Integer scheduleId, Integer roomId,
                                            List<Integer> seatIds, String promotionCode,
                                            Map<Integer, Integer> comboQuantities);

    Map<Integer, Integer> extractComboQuantities(Map<String, String> allParams);



    double calculateTotalAmount(Integer scheduleId, List<Integer> seatIds, String promotionCode);



    List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks);
    List<TheaterStockDTO> filterCombosByKeyword(String keyword,Integer roomId);
    void saveTransactionFromWebhook(SepayWebhookDTO payload);
    void finalizeBooking(Integer invoiceId);
}
