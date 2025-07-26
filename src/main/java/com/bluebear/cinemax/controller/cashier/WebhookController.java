package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.booking.BookingService;
import com.bluebear.cinemax.service.bookingSF.BookingServiceSF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private BookingService cashierBookingService;

    @Autowired
    private BookingServiceSF customerBookingService;

    @Autowired
    private InvoiceRepository invoiceRepository;


    @PostMapping("/sepay")
    public ResponseEntity<String> handleSepayPaymentWebhook(@RequestBody SepayWebhookDTO payload) {
        Integer invoiceId = null;
        try {
            String transactionContent = payload.getContent();
            if (transactionContent == null || transactionContent.isBlank()) {
                log.warn("Webhook nhận được nhưng không có nội dung giao dịch.");
                return ResponseEntity.ok("Webhook received but content is empty.");
            }

            String upperCaseContent = transactionContent.toUpperCase();
            int startIndex = upperCaseContent.indexOf("DH");
            if (startIndex == -1) {
                log.warn("Webhook nhận được nhưng không tìm thấy tiền tố 'DH'. Content: {}", transactionContent);
                return ResponseEntity.ok("Webhook received but no DH prefix found.");
            }

            String remainingString = transactionContent.substring(startIndex + 2);
            StringBuilder invoiceIdBuilder = new StringBuilder();
            for (char c : remainingString.toCharArray()) {
                if (Character.isDigit(c)) {
                    invoiceIdBuilder.append(c);
                } else {
                    break;
                }
            }

            if (invoiceIdBuilder.length() == 0) {
                log.warn("Webhook nhận được nhưng không có ID hóa đơn sau tiền tố 'DH'. Content: {}", transactionContent);
                return ResponseEntity.ok("Webhook received but invoice ID is missing after prefix.");
            }

            invoiceId = Integer.parseInt(invoiceIdBuilder.toString());
            log.info("Đã nhận webhook cho hóa đơn ID: {}", invoiceId);

            Integer finalInvoiceId = invoiceId;
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy hóa đơn với ID: " + finalInvoiceId));

            if (invoice.getCustomer() != null) {
                log.info("Xử lý hóa đơn #{} theo luồng CUSTOMER.", invoiceId);
                customerBookingService.saveTransactionFromWebhook(payload);
                customerBookingService.finalizeBooking(invoiceId);
            } else {
                // Ngược lại, đây là luồng của thu ngân (khách vãng lai hoặc nhân viên tạo).
                log.info("Xử lý hóa đơn #{} theo luồng CASHIER.", invoiceId);
                cashierBookingService.saveTransactionFromWebhook(payload);
                cashierBookingService.finalizeBooking(invoiceId);
            }

            return ResponseEntity.ok("Webhook đã được xử lý thành công cho hóa đơn " + invoiceId);

        } catch (NumberFormatException e) {
            log.error("Lỗi định dạng ID hóa đơn từ webhook. Content: {}", payload.getContent(), e);
            return ResponseEntity.badRequest().body("Invalid invoice ID format.");
        } catch (IllegalStateException e) {
            log.error("Lỗi xử lý webhook cho hóa đơn ID: {}. Lý do: {}", invoiceId, e.getMessage());
            return ResponseEntity.ok(e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi không xác định khi xử lý webhook cho hóa đơn ID: {}. Lỗi: {}", invoiceId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }
}
