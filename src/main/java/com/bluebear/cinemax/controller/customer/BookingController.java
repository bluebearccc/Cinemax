package com.bluebear.cinemax.controller.customer;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.repository.SeatRepository;
import com.bluebear.cinemax.service.booking.BookingSerivceSF;
import com.bluebear.cinemax.service.payment.VnpayService;
import com.bluebear.cinemax.service.promotion.PromotionService;
import com.bluebear.cinemax.service.seat.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private SeatService seatService;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private BookingSerivceSF bookingService;
    @Autowired
    private VnpayService vnpayService;
    @Autowired
    private SeatRepository seatRepo;
    // Trang 1: Đặt vé
    @GetMapping("")
    public String showBookingPage(@RequestParam("scheduleId") Integer scheduleId,
                                  @RequestParam("roomId") Integer roomId,

                                  Model model) {

        List<SeatDTO> seats = seatService.getSeatsWithStatus(roomId, scheduleId);
        Map<String, List<SeatDTO>> seatMap = seats.stream()
                .collect(Collectors.groupingBy(
                        seat -> seat.getPosition().substring(0, 1),
                        TreeMap::new,
                        Collectors.toList()
                ));
        double estimatedTotal = seats.stream()
                .filter(seat -> !seat.isBooked())
                .mapToDouble(seat -> seat.getUnitPrice().doubleValue())
                .sum();


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
                                     @RequestParam(value = "selectedSeats", required = false) List<Integer> seatIds,
                                     @RequestParam(value = "search", required = false) String search,
                                     Model model, RedirectAttributes redirect   ) {
        try {
            // Xử lý ghế và mã giảm giá
            // Tính sơ bộ tổng tiền mà không áp dụng mã
            double totalAmount = seatIds.stream()
                    .map(id -> seatRepo.findById(id).orElseThrow().getUnitPrice().doubleValue())
                    .mapToDouble(Double::doubleValue)
                    .sum();


            // Chuẩn bị dữ liệu cho bước 2
            List<TheaterStockDTO> combos = bookingService.getAvailableCombos();
            //locj theo ten
            if (search != null && !search.isBlank()) {
                combos = combos.stream()
                        .filter(c -> c.getFoodName().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }
            model.addAttribute("scheduleId", scheduleId);
            model.addAttribute("roomId", roomId);
            model.addAttribute("selectedSeats", seatIds);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("combos", combos);
            model.addAttribute("search", search);
            return "common/bookingFD"; // Đây là file bookingFD.html
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "  &roomId=" + roomId;
        }
    }


    // Trang 2: Đặt combo đồ ăn và hoàn tất
    @PostMapping("/step2")
    public String handleBookingStep2(@RequestParam("scheduleId") Integer scheduleId,
                                     @RequestParam("roomId") Integer roomId,
                                     @RequestParam("promotionCode") String promotionCode,
                                     @RequestParam("selectedSeats") List<Integer> seatIds,
                                     @RequestParam Map<String, String> allParams,
                                     RedirectAttributes redirect,
                                     Model model,
                                     HttpSession session) {
        try {
            Map<Integer, Integer> comboQuantities = bookingService.extractComboQuantities(allParams);
            if (promotionCode != null && !promotionCode.isBlank()) {
                Optional<PromotionDTO> promoOpt = promotionService.validatePromotionCode(promotionCode);
                if (promoOpt.isEmpty()) {
                    model.addAttribute("promotionError", "Mã giảm giá không hợp lệ hoặc đã hết hạn.");
                    promotionCode = null; // Bỏ mã lỗi để tránh áp dụng sai
                }
            }

            BookingPreviewDTO previewData = bookingService.prepareBookingPreview(scheduleId, roomId, seatIds, promotionCode, comboQuantities);



            model.addAttribute("schedule", previewData.getSchedule());
            model.addAttribute("room", previewData.getRoom());
            model.addAttribute("selectedSeats", previewData.getSelectedSeats());
            model.addAttribute("comboQuantities", previewData.getComboQuantities());
            model.addAttribute("comboList", previewData.getCombos());
            model.addAttribute("totalPrice", previewData.getTotalPrice());
            model.addAttribute("finalPrice", previewData.getFinalPrice());
            model.addAttribute("promotion", previewData.getPromotion());

            return "common/preview";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }
    @PostMapping("/confirm")
    public String confirmBookingAndRedirectToVnpay(@RequestParam("scheduleId") Integer scheduleId,
                                                   @RequestParam("roomId") Integer roomId,
                                                   @RequestParam(value = "promotionCode", required = false) String promotionCode,
                                                   @RequestParam("selectedSeats") List<Integer> seatIds,
                                                   @RequestParam Map<String, String> allParams,
                                                   HttpSession session,
                                                   RedirectAttributes redirect,
                                                   HttpServletRequest request) {
        try {
            Map<Integer, Integer> comboQuantities = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("comboQuantities[")) {
                    String idStr = key.substring(16, key.length() - 1);
                    try {
                        Integer comboId = Integer.parseInt(idStr);
                        Integer quantity = Integer.parseInt(entry.getValue());
                        if (quantity > 0) {
                            comboQuantities.put(comboId, quantity);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            InvoiceDTO invoice = bookingService.bookSeatsAndCombos(scheduleId, seatIds, promotionCode, comboQuantities);
            String vnpayUrl = vnpayService.createPaymentUrl(invoice, request);
            return "redirect:" + vnpayUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Đã có lỗi xảy ra: " + e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }


//    // API kiểm tra mã giảm giá
//    @ResponseBody
//    @GetMapping("/checkPromotion")
//    public Map<String, Object> checkPromotion(@RequestParam("code") String code,
//                                              @RequestParam("totalAmount") Double totalAmount) {
//        return bookingService.checkPromotionCode(code, totalAmount);
//    }
//
//    // API áp dụng mã giảm giá
//    @ResponseBody
//    @PostMapping("/applyPromotion")
//    public Map<String, Object> applyPromotion(@RequestParam("code") String code,
//                                              @RequestParam("totalAmount") Double totalAmount) {
//        return bookingService.applyPromotionCode(code, totalAmount);
//    }




}