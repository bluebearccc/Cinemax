package com.bluebear.cinemax.controller;
import com.bluebear.cinemax.service.EmailService;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.BookingService;
import com.bluebear.cinemax.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.bluebear.cinemax.repository.TheaterStockRepository;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
public class BookingController {
    private final EmailService emailService;
    private final BookingService bookingService;
    private final TheaterStockRepository theaterStockRepo;
    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final PromotionRepository promotionRepository;
    private final VnpayService vnpayService;
    private final InvoiceRepository invoiceRepo;
    public BookingController(EmailService emailService, BookingService bookingService, TheaterStockRepository theaterStockRepo, ScheduleRepository scheduleRepository, RoomRepository roomRepository, SeatRepository seatRepository, PromotionRepository promotionRepository, VnpayService vnpayService, InvoiceRepository invoiceRepo) {
        this.emailService = emailService;
        this.bookingService = bookingService;
        this.theaterStockRepo = theaterStockRepo;
        this.scheduleRepository = scheduleRepository;
        this.roomRepository = roomRepository;
        this.seatRepository = seatRepository;
        this.promotionRepository = promotionRepository;
        this.vnpayService = vnpayService;
        this.invoiceRepo = invoiceRepo;
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

//            InvoiceDTO invoice = bookingService.bookSeatsAndCombos(scheduleId, seatIds, promotionCode, comboQuantities);

            // 2. Lấy các thông tin chi tiết để preview
            Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(); // JPA Repo
            Room room = roomRepository.findById(roomId).orElseThrow();
            List<Seat> selectedSeats = seatRepository.findAllById(seatIds);
            List<TheaterStock> combos = theaterStockRepo.findAllById(comboQuantities.keySet());

            // 3. Tính tổng tiền
            BigDecimal totalSeatPrice = selectedSeats.stream()
                    .map(Seat::getUnitPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalComboPrice = BigDecimal.ZERO;
            for (TheaterStock combo : combos) {
                int quantity = comboQuantities.get(combo.getTheaterStockID());
                totalComboPrice = totalComboPrice.add(combo.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
            }

            BigDecimal total = totalSeatPrice.add(totalComboPrice);

            // 4. Áp dụng giảm giá nếu có
            Promotion promo = promotionRepository.findByPromotionCode(promotionCode).orElse(null);
            double discount = (promo != null && promo.isValid()) ? promo.getDiscount() / 100.0 : 0.0;
            BigDecimal finalPrice = total.multiply(BigDecimal.valueOf(1 - discount));

            // 5. Truyền dữ liệu sang view
            model.addAttribute("schedule", schedule);
            model.addAttribute("room", room);
            model.addAttribute("selectedSeats", selectedSeats);
            model.addAttribute("comboQuantities", comboQuantities);
            model.addAttribute("comboList", combos);
            model.addAttribute("totalPrice", total);
            model.addAttribute("finalPrice", finalPrice);
            model.addAttribute("promotion", promo);

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
            // Parse combo quantities từ allParams
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

            // 1. Tạo hóa đơn tạm thời (chưa thanh toán)
            Invoice invoice = bookingService.bookSeatsAndCombos(scheduleId, seatIds, promotionCode, comboQuantities);
            System.out.println("Created invoice: " + invoice.getInvoiceId() + ", total: " + invoice.getTotalPrice());
            // 2. Gọi service để tạo URL thanh toán VNPAY
            String vnpayUrl = vnpayService.createPaymentUrl(invoice,request);

            // 3. Chuyển hướng sang VNPAY
            System.out.println("Redirecting to VNPAY: " + vnpayUrl);

            return "redirect:" + vnpayUrl;

        } catch (Exception e) {
            e.printStackTrace(); // <== thêm dòng này để in lỗi ra console
            redirect.addFlashAttribute("error", "Đã có lỗi xảy ra: " + e.getMessage());
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