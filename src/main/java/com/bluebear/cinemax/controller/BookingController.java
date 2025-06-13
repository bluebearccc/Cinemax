package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.TheaterStockRepository;
import com.bluebear.cinemax.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
public class BookingController {
    private final BookingService bookingService;
    private final TheaterStockRepository theaterStockRepo;

    public BookingController(BookingService bookingService, TheaterStockRepository theaterStockRepo) {
        this.bookingService = bookingService;
        this.theaterStockRepo = theaterStockRepo;
    }

    // Trang 1: Đặt vé
    @GetMapping("")
    public String showBookingPage(@RequestParam("scheduleId") Integer scheduleId,
                                  @RequestParam("roomId") Integer roomId,
                                  Model model) {
        List<SeatDTO> seats = bookingService.getSeatsWithStatus(roomId, scheduleId);
        Map<String, List<SeatDTO>> seatMap = seats.stream()
                .collect(Collectors.groupingBy(
                        seat -> seat.getPosition().substring(0, 1),
                        TreeMap::new,
                        Collectors.toList()
                ));
        BigDecimal estimatedTotal = seats.stream()
                .filter(seat -> !seat.isBooked())
                .map(SeatDTO::getUnitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalAmount", estimatedTotal);
        model.addAttribute("seatMap", seatMap);
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("roomId", roomId);


        return "common/booking"; // Đây là file booking.html
    }

    // Xử lý bước 1 và chuyển sang bước 2
    @PostMapping("/step1")
    public String handleBookingStep1(@RequestParam("scheduleId") Integer scheduleId,
                                     @RequestParam("roomId") Integer roomId,
                                     @RequestParam("selectedSeats") List<Integer> seatIds,
                                     @RequestParam(value = "promotionCode", required = false) String promotionCode,
                                     Model model, RedirectAttributes redirect) {
        try {
            // Xử lý ghế và mã giảm giá
            BigDecimal totalAmount = bookingService.calculateTotalAmount(scheduleId, seatIds, promotionCode);

            // Chuẩn bị dữ liệu cho bước 2
            List<TheaterStock> combos = theaterStockRepo.findByStatus("active");

            model.addAttribute("scheduleId", scheduleId);
            model.addAttribute("roomId", roomId);
            model.addAttribute("selectedSeats", seatIds);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("promotionCode", promotionCode);
            model.addAttribute("combos", combos);

            return "common/bookingFD"; // Đây là file bookingFD.html
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }

    // Trang 2: Đặt combo đồ ăn và hoàn tất
    @PostMapping("/step2")
    public String handleBookingStep2(@RequestParam("scheduleId") Integer scheduleId,
                                     @RequestParam("roomId") Integer roomId,
                                     @RequestParam("selectedSeats") List<Integer> seatIds,
                                     @RequestParam("promotionCode") String promotionCode,
                                     @RequestParam Map<Integer, Integer> comboQuantities, // Map chứa combo và số lượng
                                     RedirectAttributes redirect) {
        try {
            // Gọi service để đặt chỗ và combo
            Invoice invoice = bookingService.bookSeatsAndCombos(scheduleId, seatIds, promotionCode, comboQuantities);

            // Thành công, chuyển hướng sang hóa đơn
            redirect.addFlashAttribute("success", "Đặt vé và combo thành công!");
            return "redirect:/invoices/" + invoice.getInvoiceId();
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }

    // API kiểm tra mã giảm giá
    @ResponseBody
    @GetMapping("/checkPromotion")
    public Map<String, Object> checkPromotion(@RequestParam("code") String code,
                                              @RequestParam("totalAmount") BigDecimal totalAmount) {
        return bookingService.checkPromotionCode(code, totalAmount);
    }

    // API áp dụng mã giảm giá
    @ResponseBody
    @PostMapping("/applyPromotion")
    public Map<String, Object> applyPromotion(@RequestParam("code") String code,
                                              @RequestParam("totalAmount") BigDecimal totalAmount) {
        return bookingService.applyPromotionCode(code, totalAmount);
    }
}