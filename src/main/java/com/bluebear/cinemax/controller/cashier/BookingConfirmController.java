package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.PaymentMethod;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.booking.BookingService;
import com.bluebear.cinemax.service.theaterstock.TheaterStockService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cashier")
public class BookingConfirmController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private TheaterStockService theaterStockService;

    @Value("${payment.sepay.bank}")
    private String sepayBank;
    @Value("${payment.sepay.account}")
    private String sepayAccount;

    @PostMapping("/booking/confirm")
    public String confirmBooking(@ModelAttribute BookingRequestDTO bookingRequest, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            bookingRequest.setPaymentMethod(PaymentMethod.CASH.name());
            BookingResultDTO result = bookingService.createBooking(bookingRequest);
            model.addAttribute("bookingResult", result);
            session.setAttribute("bookingResult", result);
            model.addAttribute("currentStep", 5);
            return "cashier/cashier-booking";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Fail to book ticket " + e.getMessage());
            return "redirect:/cashier/back-to-seats";
        }
    }

    @PostMapping("/booking/initiate-bank-payment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> initiateBankPayment(@RequestBody BookingRequestDTO bookingRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            bookingRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER.name());
            InvoiceDTO invoiceDto = bookingService.initiateBooking(bookingRequest);
            if (invoiceDto == null) {
                throw new Exception("Error initializing invoice.");
            }
            String qrContent = String.format("DH%d", invoiceDto.getInvoiceID());
            String vietQRTemplate = "https://img.vietqr.io/image/%s-%s-print.png?amount=%d&addInfo=%s";
            String qrUrl = String.format(vietQRTemplate,
                    sepayBank,
                    sepayAccount,
                    invoiceDto.getTotalPrice().longValue(),
                    URLEncoder.encode(qrContent, StandardCharsets.UTF_8)
            );

            response.put("success", true);
            response.put("qrUrl", qrUrl);
            response.put("invoiceId", invoiceDto.getInvoiceID());
            response.put("amount", invoiceDto.getTotalPrice());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error creating QR Code: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== PHƯƠNG THỨC MỚI ĐỂ PHÂN TRANG ĐỒ ĂN ====================
    @GetMapping("/food-fragment")
    public String getFoodFragment(@RequestParam(required = false) String foodKeyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  HttpSession session,
                                  Model model) {
        Integer theaterId = (Integer) session.getAttribute("theaterId");
        if (theaterId != null) {
            Pageable foodPageable = PageRequest.of(page, 4); // 4 items per page
            Page<TheaterStockDTO> theaterStockPage = theaterStockService.findAvailableByTheaterIdAndKeyword(theaterId, foodKeyword, foodPageable);
            model.addAttribute("theaterStockPage", theaterStockPage);
        }
        // Trả về một fragment của HTML, không phải cả trang
        return "cashier/cashier-booking :: foodListFragment";
    }

    @PostMapping("/booking/check-payment-status")
    @ResponseBody
    public Map<String, String> checkPaymentStatus(@RequestParam("order_id") Integer orderId) {
        Map<String, String> response = new HashMap<>();
        try {
            Invoice invoice = invoiceRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Invoice ID"));
            response.put("payment_status", invoice.getStatus().name());
        } catch (Exception e) {
            response.put("payment_status", "Unpaid");
        }
        return response;
    }

    @GetMapping("/booking/success/{invoiceId}")
    public String showSuccessPage(@PathVariable Integer invoiceId, HttpSession session, Model model) {
        try {
            BookingResultDTO result = bookingService.getBookingResult(invoiceId);
            model.addAttribute("currentStep", 5);
            model.addAttribute("bookingResult", result);
            session.setAttribute("bookingResult", result);
        } catch (Exception e) {
            model.addAttribute("currentStep", 5);
            model.addAttribute("bookingResult", null);
            model.addAttribute("errorMessage", "Error loading invoice info");
        }
        return "cashier/cashier-booking";
    }
}