package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.service.booking.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handleSepayPaymentWebhook(@RequestBody SepayWebhookDTO payload) {
        try {
            bookingService.saveTransactionFromWebhook(payload);
            String transactionContent = payload.getContent();
            if (transactionContent == null || transactionContent.isBlank()) {
                return ResponseEntity.ok("Webhook received but content is empty.");
            }

            String upperCaseContent = transactionContent.toUpperCase();
            int startIndex = upperCaseContent.indexOf("DH");
            if (startIndex == -1) {
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
                return ResponseEntity.ok("Webhook received but invoice ID is missing after prefix.");
            }

            Integer invoiceId = Integer.parseInt(invoiceIdBuilder.toString());
            bookingService.finalizeBooking(invoiceId);
            return ResponseEntity.ok("Webhook processed successfully.");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid invoice ID format.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }
}