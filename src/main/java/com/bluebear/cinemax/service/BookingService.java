package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
    private final SeatRepository seatRepo;
    private final DetailSeatRepository detailSeatRepo;
    private final PromotionRepository promotionRepo;
    private final InvoiceRepository invoiceRepo;
    private final ScheduleRepository scheduleRepo;
    private final CustomerRepository customerRepo;
    private final TheaterStockRepository theaterStockRepo;
    private final DetailFDRepository detailFDRepo;
    public BookingService(CustomerRepository customerRepo, SeatRepository seatRepo, DetailSeatRepository detailSeatRepo, PromotionRepository promotionRepo, InvoiceRepository invoiceRepo, ScheduleRepository scheduleRepo, TheaterStockRepository theaterStockRepo, DetailFDRepository detailFDRepo) {
        this.seatRepo = seatRepo;
        this.detailSeatRepo = detailSeatRepo;
        this.promotionRepo = promotionRepo;
        this.invoiceRepo = invoiceRepo;
        this.scheduleRepo = scheduleRepo;
        this.customerRepo = customerRepo;
        this.theaterStockRepo = theaterStockRepo;
        this.detailFDRepo = detailFDRepo;
    }

    public List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId) {
        List<Seat> seats = seatRepo.findByRoomRoomId(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat : seats) {
            boolean booked = detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId);
            SeatDTO dto = new SeatDTO();
            dto.setSeatId(seat.getSeatId());
            dto.setPosition(seat.getPosition());
            dto.setSeatType(seat.getSeatType());
            dto.setVIP(seat.isVIP());
            dto.setUnitPrice(seat.getUnitPrice());
            dto.setBooked(booked);
            seatDTOs.add(dto);
        }

        return seatDTOs;
    }
    public Optional<Promotion> validatePromotionCode(String code) {
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(code);
        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                return promoOpt;
            }
        }
        return Optional.empty();
    }
    public Invoice bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos) {
        BigDecimal total = BigDecimal.ZERO;

        List<Seat> seats = seatRepo.findAllById(seatIds);
        for (Seat seat : seats) {
            boolean booked = detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId);
            if (booked) {
                throw new IllegalStateException("Một số ghế đã được đặt.");
            }
            total = total.add(seat.getUnitPrice());
        }

        Optional<Promotion> promoOpt = validatePromotionCode(promotionCode);
        Promotion promo = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (promoOpt.isPresent()) {
            promo = promoOpt.get();
            discount = total.multiply(BigDecimal.valueOf(promo.getDiscount())).divide(BigDecimal.valueOf(100));
            total = total.subtract(discount);
            promo.setQuantity(promo.getQuantity() - 1);
            promotionRepo.save(promo);
        }

        Optional<Customer> cus = customerRepo.findById(1);
        if (cus.isEmpty()) throw new IllegalStateException("Không tìm thấy khách hàng.");

        Invoice invoice = new Invoice();
        invoice.setCustomer(cus.get());
        invoice.setPromotion(promo);
        invoice.setDiscount(discount.floatValue());
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.Cancelled); // chưa thanh toán

        // Tạm set tổng trước combo
        invoice.setTotalPrice(total);
        invoice = invoiceRepo.save(invoice);

        for (Seat seat : seats) {
            detailSeatRepo.insertDetailSeat(invoice.getInvoiceId(), seat.getSeatId(), scheduleId,"Booked");
        }

        // Xử lý combo
        BigDecimal comboTotal = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> combo : selectedCombos.entrySet()) {
            Integer theaterStockId = combo.getKey();
            Integer quantity = combo.getValue();

            TheaterStock stock = theaterStockRepo.findById(theaterStockId)
                    .orElseThrow(() -> new IllegalArgumentException("Combo không tồn tại: " + theaterStockId));

            if (stock.getQuantity() < quantity) {
                throw new IllegalStateException("Không đủ hàng cho combo: " + stock.getFoodName());
            }

            stock.setQuantity(stock.getQuantity() - quantity);
            theaterStockRepo.save(stock);

            BigDecimal comboTotalPrice = stock.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
            comboTotal = comboTotal.add(comboTotalPrice);

            DetailFD detailFD = new DetailFD();
            detailFD.setInvoice(invoice);
            detailFD.setTheaterStock(stock);
            detailFD.setQuantity(quantity);
            detailFD.setTotalPrice(comboTotalPrice);
            detailFD.setStatus("Booked");
            detailFDRepo.save(detailFD);
        }

        // Cập nhật tổng giá sau combo
        total = total.add(comboTotal);
        invoice.setTotalPrice(total);
        invoice = invoiceRepo.save(invoice); // update lại

        return invoice;
    }

    public Map<String, Object> checkPromotionCode(String code, BigDecimal totalAmount) {
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                BigDecimal discount = totalAmount.multiply(BigDecimal.valueOf(promo.getDiscount())).divide(BigDecimal.valueOf(100));
                response.put("valid", true);
                response.put("discount", discount);
                response.put("message", "Mã hợp lệ! Bạn được giảm: " + discount + " VNĐ.");
            } else {
                response.put("valid", false);
                response.put("message", "Mã giảm giá đã hết hạn hoặc không còn khả dụng.");
            }
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại.");
        }

        return response;
    }
    @Transactional
    public Map<String, Object> applyPromotionCode(String code, BigDecimal totalAmount) {
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();

            // Kiểm tra thời hạn và số lượng mã
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                BigDecimal discount = totalAmount.multiply(BigDecimal.valueOf(promo.getDiscount())).divide(BigDecimal.valueOf(100));

                // Cập nhật số lượng mã giảm giá
                promo.setQuantity(promo.getQuantity() - 1);
                promotionRepo.save(promo);

                response.put("valid", true);
                response.put("discount", discount);
                response.put("message", "Mã áp dụng thành công! Bạn được giảm: " + discount + " VNĐ.");
            } else {
                response.put("valid", false);
                response.put("message", "Mã giảm giá đã hết hạn hoặc không còn khả dụng.");
            }
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại.");
        }

        return response;
    }
    public BigDecimal calculateTotalAmount(Integer scheduleId, List<Integer> seatIds, String promotionCode) {
        // Lấy danh sách DetailSeat theo scheduleId
        List<DetailSeat> detailSeats = detailSeatRepo.findByScheduleScheduleId(scheduleId);

        // Lọc ra các ghế có ID trong danh sách `seatIds`
        List<Seat> seats = detailSeats.stream()
                .filter(detailSeat -> seatIds.contains(detailSeat.getSeat().getSeatId()))
                .map(DetailSeat::getSeat)
                .toList();

        // Tính tổng tiền ghế
        BigDecimal totalSeatAmount = seats.stream()
                .map(Seat::getUnitPrice) // Lấy giá từng ghế
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Tổng tiền các ghế

        // Nếu không có mã giảm giá, trả về tổng tiền ghế
        if (promotionCode == null || promotionCode.isBlank()) {
            return totalSeatAmount;
        }

        // Kiểm tra mã giảm giá
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode);
        if (promoOpt.isEmpty()) {
            throw new IllegalStateException("Mã giảm giá không hợp lệ.");
        }

        Promotion promotion = promoOpt.get();

        // Kiểm tra thời hạn và số lượng mã
        if (!promotion.isValid()) {
            throw new IllegalStateException("Mã giảm giá đã hết hạn hoặc không còn khả dụng.");
        }

        // Tính tiền giảm giá
        BigDecimal discountAmount = totalSeatAmount.multiply(BigDecimal.valueOf(promotion.getDiscount()))
                .divide(BigDecimal.valueOf(100));

        // Trả về tổng tiền sau khi áp dụng mã giảm giá
        return totalSeatAmount.subtract(discountAmount).max(BigDecimal.ZERO); // Đảm bảo không âm
    }
    @Transactional
    public Invoice bookSeatsTemp(Integer scheduleId, List<Integer> seatIds, String promotionCode) {
        BigDecimal total = BigDecimal.ZERO;
        List<Seat> seats = seatRepo.findAllById(seatIds);

        for (Seat seat : seats) {
            boolean booked = detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId);
            if (booked) {
                throw new IllegalStateException("Một số ghế đã được đặt.");
            }
            total = total.add(seat.getUnitPrice());
        }

        Optional<Promotion> promoOpt = validatePromotionCode(promotionCode);
        Promotion promo = null;
        BigDecimal discount = BigDecimal.ZERO;

        if (promoOpt.isPresent()) {
            promo = promoOpt.get();
            discount = total.multiply(BigDecimal.valueOf(promo.getDiscount())).divide(BigDecimal.valueOf(100));
            total = total.subtract(discount);
            promo.setQuantity(promo.getQuantity() - 1);
            promotionRepo.save(promo);
        }

        Optional<Customer> cus = customerRepo.findById(1); // Cần chỉnh nếu có login
        if (cus.isEmpty()) throw new IllegalStateException("Khách hàng không tồn tại.");

        Invoice invoice = new Invoice();
        invoice.setCustomer(cus.get());
        invoice.setPromotion(promo);
        invoice.setDiscount(discount.floatValue());
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setTotalPrice(total);
        invoice = invoiceRepo.save(invoice);

        // Lưu chi tiết ghế
        for (Seat seat : seats) {
            detailSeatRepo.insertDetailSeat(invoice.getInvoiceId(), seat.getSeatId(),  scheduleId,"Booked");
        }

        return invoice;
    }
}
