package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.BookingResultDTO;
import com.bluebear.cinemax.service.booking.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cashier/print")
public class PrintController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/invoice/{invoiceId}")
    public String printInvoice(@PathVariable Integer invoiceId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            BookingResultDTO bookingResult = (BookingResultDTO) session.getAttribute("bookingResult");
            if (bookingResult == null || !invoiceId.equals(bookingResult.getInvoiceId())) {
                bookingResult = bookingService.getBookingResult(invoiceId);
            }
            if (bookingResult == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invoice not found");
                return "redirect:/cashier/";
            }
            model.addAttribute("bookingResult", bookingResult);
            return "cashier/print-ticket";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error to print Invoice " + e.getMessage());
            return "redirect:/cashier/";
        }
    }

    @GetMapping("/food-receipt/{invoiceId}")
    public String printFoodInvoice(@PathVariable Integer invoiceId, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            BookingResultDTO bookingResult = (BookingResultDTO) session.getAttribute("bookingResult");
            if (bookingResult == null || !invoiceId.equals(bookingResult.getInvoiceId())) {
                bookingResult = bookingService.getBookingResult(invoiceId);
            }
            if (bookingResult == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Invoice not found");
                return "redirect:/cashier/";
            }
            model.addAttribute("bookingResult", bookingResult);
            return "cashier/print-food-receipt";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error to find invoice " + e.getMessage());
            return "redirect:/cashier/";
        }
    }
}