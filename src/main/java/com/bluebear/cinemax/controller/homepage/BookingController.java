package com.bluebear.cinemax.controller.homepage;
import com.bluebear.cinemax.dto.*;

import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import com.bluebear.cinemax.service.bookingSF.BookingServiceSF;
import com.bluebear.cinemax.service.VnpayService;
import com.bluebear.cinemax.service.promotion.PromotionService;
import com.bluebear.cinemax.service.seat.SeatService;
import com.bluebear.cinemax.service.theater.TheaterService;
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
    private BookingServiceSF bookingService;
    @Autowired
    private VnpayService vnpayService;
    @Autowired
    private SeatRepository seatRepo;
    @Autowired
    private InvoiceRepository invoiceRepo;
    @Autowired
    private TheaterService theaterService;
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
        List<Integer> unpaidSeatIds = seatService.getUnpaidSeatIdsForSchedule(scheduleId);

        model.addAttribute("totalAmount", estimatedTotal);
        model.addAttribute("seatMap", seatMap);
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("unpaidSeatIds", unpaidSeatIds);

        return "common/booking"; // Đây là file booking.html
    }

    // Xử lý bước 1 và chuyển sang bước 2
    @PostMapping("/step1")
    public String handleBookingStep1(@RequestParam("scheduleId") Integer scheduleId,
                                     @RequestParam("roomId") Integer roomId,
                                     @RequestParam(value = "selectedSeats", required = false) List<Integer> seatIds,
                                     @RequestParam(value = "search", required = false) String search,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model, RedirectAttributes redirect) {
        try {
            // Xử lý ghế và mã giảm giá
            // Tính sơ bộ tổng tiền mà không áp dụng mã
            double totalAmount = seatIds.stream()
                    .map(id -> seatRepo.findById(id).orElseThrow().getUnitPrice().doubleValue())
                    .mapToDouble(Double::doubleValue)
                    .sum();


            // Chuẩn bị dữ liệu cho bước 2
            List<TheaterStockDTO> combos = bookingService.getAvailableCombos(roomId);
            //locj theo ten
            if (search != null && !search.isBlank()) {
                combos = combos.stream()
                        .filter(c -> c.getFoodName().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            }
            int pageSize = 6;
            int totalItems = combos.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            int fromIndex = (page - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalItems);
            List<TheaterStockDTO> combosPage = combos.subList(fromIndex, toIndex);


            model.addAttribute("scheduleId", scheduleId);
            model.addAttribute("roomId", roomId);
            model.addAttribute("selectedSeats", seatIds);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("combos", combos);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            return "common/bookingFD";
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
                    promotionCode = null;
                }
            }

            CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
            BookingPreviewDTO previewData = bookingService.prepareBookingPreview(scheduleId, roomId, seatIds, promotionCode, comboQuantities);
            InvoiceDTO tempInvoice = bookingService.createTemporaryInvoice(previewData, customerDTO.getId());
            String qrUrl = vnpayService.createSepayQrUrl(tempInvoice);

            model.addAttribute("qrUrl", qrUrl);
            model.addAttribute("invoiceId", tempInvoice.getInvoiceID());


            model.addAttribute("schedule", previewData.getSchedule());
            model.addAttribute("room", previewData.getRoom());
            model.addAttribute("selectedSeats", previewData.getSelectedSeats());
            model.addAttribute("comboQuantities", previewData.getComboQuantities());
            model.addAttribute("comboList", previewData.getCombos());
            model.addAttribute("totalPrice", previewData.getTotalPrice());
            model.addAttribute("finalPrice", previewData.getFinalPrice());
            model.addAttribute("promotion", previewData.getPromotion());
            System.out.println("🧾 Invoice ID trên QR: " + tempInvoice.getInvoiceID());
            return "common/preview";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("error", "Một hoặc nhiều ghế bạn chọn đã được người khác đặt trước đó. Vui lòng chọn lại ghế khác.");
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }
    @PostMapping("/confirm")
    public String confirmBookingAndRedirectToVnpay(@RequestParam("invoiceId") Integer invoiceId,
                                                   @RequestParam("scheduleId") Integer scheduleId,
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

            InvoiceDTO invoice = bookingService.getInvoiceById(invoiceId);
            String vnpayUrl = vnpayService.createPaymentUrl(invoice, request);
            return "redirect:" + vnpayUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirect.addFlashAttribute("error", "Đã có lỗi xảy ra: " + e.getMessage());
            return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
        }
    }
    @GetMapping("/confirm-success")
    public String handleSepaySuccessRedirect(@RequestParam("invoiceId") Integer invoiceId,
                                             Model model) {
        InvoiceDTO invoice = bookingService.getInvoiceById(invoiceId);
        BookingPreviewDTO preview = bookingService.reconstructBookingPreview(invoiceId);
        model.addAttribute("schedule", preview.getSchedule());
        model.addAttribute("room", preview.getRoom());
        model.addAttribute("theater", theaterService.getTheaterById(preview.getRoom().getTheaterID()));
        model.addAttribute("selectedSeats", preview.getSelectedSeats());
        model.addAttribute("comboQuantities", preview.getComboQuantities());
        model.addAttribute("comboList", preview.getCombos());
        model.addAttribute("promotion", preview.getPromotion());
        model.addAttribute("totalPrice", preview.getTotalPrice());
        model.addAttribute("finalPrice", preview.getFinalPrice());

        return "common/confirm";
    }
    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam Integer invoiceId,
                                @RequestParam Integer scheduleId,
                                @RequestParam Integer roomId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        bookingService.cancelInvoice(invoiceId);

        session.removeAttribute("selectedSeats");
        session.removeAttribute("comboQuantities");
        session.removeAttribute("promotion");

        redirectAttributes.addAttribute("scheduleId", scheduleId);
        redirectAttributes.addAttribute("roomId", roomId);
        return "redirect:/booking?scheduleId=" + scheduleId + "&roomId=" + roomId;
    }
    @GetMapping("/check-status")
    @ResponseBody
    public Map<String, String> checkInvoiceStatus(@RequestParam("invoiceId") Integer invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
        if (invoice != null && invoice.getStatus() == InvoiceStatus.Booked) {
            return Map.of("status", "BOOKED");
        }
        return Map.of("status", "PENDING");
    }
    @GetMapping("/simulate-payment")
    @ResponseBody
    public String simulateWebhook(@RequestParam Integer invoiceId) {
        bookingService.finalizeBooking(invoiceId);
        return "OK";
    }


}