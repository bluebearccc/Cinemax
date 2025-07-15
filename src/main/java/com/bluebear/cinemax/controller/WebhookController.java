package com.bluebear.cinemax.controller;
import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.enumtype.DetailFD_Status;
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
        log.info("✅ Received Sepay webhook: {}", payload);

        try {
            bookingService.saveTransactionFromWebhook(payload);

            String content = payload.getContent();
            log.info("📩 Webhook Content: {}", content);

            if (content == null || content.isBlank()) {
                log.warn("⚠️ Webhook content is empty");
                return ResponseEntity.ok("Content is empty.");
            }

            String upperContent = content.toUpperCase();
            int dhIndex = upperContent.indexOf("DH");
            log.info("🔍 Index of 'DH' in content: {}", dhIndex);

            if (dhIndex == -1) {
                log.warn("⚠️ No 'DH' prefix found in content: {}", content);
                return ResponseEntity.ok("No invoice ID found.");
            }

            String remaining = content.substring(dhIndex + 2);
            log.info("📌 Remaining string after 'DH': {}", remaining);

            StringBuilder idBuilder = new StringBuilder();
            for (char ch : remaining.toCharArray()) {
                if (Character.isDigit(ch)) {
                    idBuilder.append(ch);
                } else {
                    break;
                }
            }

            log.info("🔢 Extracted Invoice ID String: {}", idBuilder);

            if (idBuilder.length() == 0) {
                log.warn("⚠️ No digits found after 'DH' in content: {}", content);
                return ResponseEntity.ok("Invalid invoice ID format.");
            }

            Integer invoiceId = Integer.parseInt(idBuilder.toString());
            log.info("✅ Parsed Invoice ID: {}", invoiceId);

            bookingService.finalizeBooking(invoiceId);

            log.info("✅ Booking finalized successfully for Invoice ID: {}", invoiceId);
            return ResponseEntity.ok("Payment processed successfully for invoice ID: " + invoiceId);

        } catch (NumberFormatException e) {
            log.error("❌ Failed to parse invoice ID", e);
            return ResponseEntity.badRequest().body("Invalid invoice ID format.");
        } catch (Exception e) {
            log.error("❌ Error during webhook processing", e);
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage()); // <--- DEBUG ONLY
        }
    }



}
