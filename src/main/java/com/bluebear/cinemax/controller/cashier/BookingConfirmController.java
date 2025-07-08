package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.BookingRequestDTO;
import com.bluebear.cinemax.dto.BookingResultDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.PaymentMethod;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.booking.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cashier/booking")
public class BookingConfirmController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Value("${payment.sepay.bank}")
    private String sepayBank;
    @Value("${payment.sepay.account}")
    private String sepayAccount;

    @PostMapping("/confirm")
    public String confirmBooking(BookingRequestDTO bookingRequest, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            if ("bank".equalsIgnoreCase(bookingRequest.getPaymentMethod())) {
                bookingRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER.name());
                InvoiceDTO invoiceDto = bookingService.initiateBooking(bookingRequest);
                if (invoiceDto == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi khởi tạo hoá đơn. Vui lòng thử lại.");
                    return "redirect:/cashier/back-to-seats";
                }
                model.addAttribute("invoiceDto", invoiceDto);
                model.addAttribute("sepayBank", sepayBank);
                model.addAttribute("sepayAccount", sepayAccount);
                return "cashier/index";
            } else {
                BookingResultDTO result = bookingService.createBooking(bookingRequest);
                model.addAttribute("bookingResult", result);
                session.setAttribute("bookingResult", result);
                model.addAttribute("currentStep", 5);
                return "cashier/cashier-booking";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi đặt vé: " + e.getMessage());
            return "redirect:/cashier/back-to-seats";
        }
    }

    @GetMapping("/success/{invoiceId}")
    public ModelAndView showSuccessPage(@PathVariable Integer invoiceId, HttpSession session) {
        ModelAndView mav = new ModelAndView("cashier/cashier-booking");
        try {
            BookingResultDTO result = bookingService.getBookingResult(invoiceId);
            mav.addObject("currentStep", 5);
            mav.addObject("bookingResult", result);
            session.setAttribute("bookingResult", result);
        } catch (Exception e) {
            mav.addObject("currentStep", 5);
            mav.addObject("bookingResult", null);
            mav.addObject("errorMessage", "Không thể tải thông tin đặt vé.");
        }
        return mav;
    }

    @PostMapping("/process-payment")
    public ModelAndView processPayment(BookingRequestDTO bookingRequest) {
        if (PaymentMethod.BANK_TRANSFER.name().equals(bookingRequest.getPaymentMethod())) {
            InvoiceDTO invoiceDto = bookingService.initiateBooking(bookingRequest);
            ModelAndView mav = new ModelAndView("payment-qr");
            mav.addObject("invoiceDto", invoiceDto);
            mav.addObject("sepayBank", sepayBank);
            mav.addObject("sepayAccount", sepayAccount);
            return mav;
        }
        return new ModelAndView("redirect:/cashier");
    }

    @PostMapping("/check-payment-status")
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
}