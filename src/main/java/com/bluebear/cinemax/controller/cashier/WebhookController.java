package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.service.booking.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final BookingService bookingService;

    @PostMapping("/sepay")
    public ResponseEntity<String> handleSepayPaymentWebhook(@RequestBody SepayWebhookDTO payload) {
        log.info("Received SePay webhook with raw content: {}", payload.getContent());
        try {
            bookingService.saveTransactionFromWebhook(payload);
            String transactionContent = payload.getContent();
            if (transactionContent == null || transactionContent.isBlank()) {
                log.warn("Webhook received with empty content.");
                return ResponseEntity.ok("Webhook received but content is empty.");
            }
            String upperCaseContent = transactionContent.toUpperCase();
            int startIndex = upperCaseContent.indexOf("DH");
            if (startIndex == -1) {
                log.warn("Webhook content does not contain invoice prefix 'DH': {}", transactionContent);
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
                log.warn("'DH' prefix found but no invoice number followed: {}", transactionContent);
                return ResponseEntity.ok("Webhook received but invoice ID is missing after prefix.");
            }

            Integer invoiceId = Integer.parseInt(invoiceIdBuilder.toString());

            log.info("Extracted Invoice ID: {}. Proceeding to finalize booking.", invoiceId);
            bookingService.finalizeBooking(invoiceId); // Hoàn tất đơn hàng

            log.info("Successfully finalized booking for Invoice ID: {}", invoiceId);
            return ResponseEntity.ok("Webhook processed successfully.");

        } catch (NumberFormatException e) {
            log.error("Could not parse Invoice ID from content: '{}'", payload.getContent(), e);
            return ResponseEntity.badRequest().body("Invalid invoice ID format.");
        } catch (Exception e) {
            log.error("Error processing webhook for content: '{}'", payload.getContent(), e);
            return ResponseEntity.internalServerError().body("An internal error occurred.");
        }
    }
}