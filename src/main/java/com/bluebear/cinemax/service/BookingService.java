package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.Invoice_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
        List<Seat> seats = seatRepo.findByRoomRoomID(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat : seats) {
            boolean booked = detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId);
            SeatDTO dto = new SeatDTO();
            dto.setSeatID(seat.getSeatId());
            dto.setPosition(seat.getPosition());
            dto.setSeatType(TypeOfSeat.valueOf(seat.getSeatType()));
            dto.setIsVIP(seat.isVIP());
            dto.setUnitPrice(seat.getUnitPrice());
            dto.setBooked(booked);
            seatDTOs.add(dto);
        }

        return seatDTOs;
    }

    public Optional<PromotionDTO> validatePromotionCode(String code) {
        return promotionRepo.findByPromotionCode(code)
                .filter(promo -> promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0)
                .map(promo -> new PromotionDTO(promo.getPromotionID(), promo.getPromotionCode(), promo.getDiscount(), promo.getEndTime(), promo.getQuantity()));
    }

    @Transactional
    public InvoiceDTO bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos) {
        double total = 0.0;

        List<Seat> seats = seatRepo.findAllById(seatIds);
        for (Seat seat : seats) {
            if (detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId)) {
                throw new IllegalStateException("Một số ghế đã được đặt.");
            }
            total += seat.getUnitPrice().doubleValue();
        }

        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode)
                .filter(p -> p.getEndTime().isAfter(LocalDateTime.now()) && p.getQuantity() > 0);
        Promotion promo = null;
        double discount = 0.0;

        if (promoOpt.isPresent()) {
            promo = promoOpt.get();
            discount = total * promo.getDiscount() / 100.0;
            total -= discount;
            promo.setQuantity(promo.getQuantity() - 1);
            promotionRepo.save(promo);
        }

        Customer customer = customerRepo.findById(1).orElseThrow(() -> new IllegalStateException("Không tìm thấy khách hàng."));

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setPromotion(promo);
        invoice.setDiscount((float) discount);
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setStatus(Invoice_Status.Cancelled);
        invoice.setTotalPrice(total);
        invoice = invoiceRepo.save(invoice);

        for (Seat seat : seats) {
            detailSeatRepo.insertDetailSeat(invoice.getInvoiceId(), seat.getSeatId(), scheduleId, "Booked");
        }

        double comboTotal = 0.0;
        for (Map.Entry<Integer, Integer> combo : selectedCombos.entrySet()) {
            TheaterStock stock = theaterStockRepo.findById(combo.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Combo không tồn tại: " + combo.getKey()));

            if (stock.getQuantity() < combo.getValue()) {
                throw new IllegalStateException("Không đủ hàng cho combo: " + stock.getFoodName());
            }

            stock.setQuantity(stock.getQuantity() - combo.getValue());
            theaterStockRepo.save(stock);

            double comboTotalPrice = stock.getUnitPrice().doubleValue()*1000 * combo.getValue();
            comboTotal += comboTotalPrice;

            DetailFD detailFD = new DetailFD();
            detailFD.setInvoice(invoice);
            detailFD.setTheaterStock(stock);
            detailFD.setQuantity(combo.getValue());
            detailFD.setTotalPrice(comboTotalPrice);
            detailFD.setStatus("Booked");
            detailFDRepo.save(detailFD);
        }

        total += comboTotal;
        invoice.setTotalPrice(total);

        invoice = invoiceRepo.save(invoice);

        return new InvoiceDTO(invoice);
    }


    public Map<String, Object> checkPromotionCode(String code, double totalAmount) {
        Optional<PromotionDTO> promoOpt = validatePromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            PromotionDTO promo = promoOpt.get();
            double discount = totalAmount * promo.getDiscount() / 100.0;
            response.put("valid", true);
            response.put("discount", discount);
            response.put("message", "Mã hợp lệ! Bạn được giảm: " + discount + " VNĐ.");
        } else {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại hoặc đã hết hạn.");
        }
        return response;
    }
    @Transactional
    public Map<String, Object> applyPromotionCode(String code, double totalAmount) {
        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(code);
        Map<String, Object> response = new HashMap<>();

        if (promoOpt.isPresent()) {
            Promotion promo = promoOpt.get();

            // Kiểm tra thời hạn và số lượng mã
            if (promo.getEndTime().isAfter(LocalDateTime.now()) && promo.getQuantity() > 0) {
                double discount = totalAmount * promo.getDiscount() / 100.0;

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

    public double calculateTotalAmount(Integer scheduleId, List<Integer> seatIds, String promotionCode) {
        List<Seat> seats = seatRepo.findAllById(seatIds);
        double totalSeatAmount = seats.stream()
                .mapToDouble(seat -> seat.getUnitPrice().doubleValue())
                .sum();

        if (promotionCode == null || promotionCode.isBlank()) {
            return totalSeatAmount;
        }

        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode)
                .filter(p -> p.getEndTime().isAfter(LocalDateTime.now()) && p.getQuantity() > 0);

        if (promoOpt.isEmpty()) {
            throw new IllegalStateException("Mã giảm giá không hợp lệ hoặc hết hạn.");
        }

        Promotion promo = promoOpt.get();
        double discount = totalSeatAmount * promo.getDiscount() / 100.0;
        return Math.max(totalSeatAmount - discount, 0);
    }
    @Transactional
    public InvoiceDTO bookSeatsTemp(Integer scheduleId, List<Integer> seatIds, String promotionCode) {
        double total = 0.0;
        List<Seat> seats = seatRepo.findAllById(seatIds);

        for (Seat seat : seats) {
            if (detailSeatRepo.existsBySeatSeatIdAndScheduleScheduleId(seat.getSeatId(), scheduleId)) {
                throw new IllegalStateException("Một số ghế đã được đặt.");
            }
            total += seat.getUnitPrice().doubleValue();
        }

        Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode)
                .filter(p -> p.getEndTime().isAfter(LocalDateTime.now()) && p.getQuantity() > 0);
        Promotion promo = null;
        double discount = 0.0;

        if (promoOpt.isPresent()) {
            promo = promoOpt.get();
            discount = total * promo.getDiscount() / 100.0;
            total -= discount;
            promo.setQuantity(promo.getQuantity() - 1);
            promotionRepo.save(promo);
        }

        Customer customer = customerRepo.findById(1).orElseThrow(() -> new IllegalStateException("Không tìm thấy khách hàng."));

        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setPromotion(promo);
        invoice.setDiscount((float) discount);
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setTotalPrice(total);
        invoice.setStatus(Invoice_Status.Cancelled);
        invoice = invoiceRepo.save(invoice);

        for (Seat seat : seats) {
            detailSeatRepo.insertDetailSeat(invoice.getInvoiceId(), seat.getSeatId(), scheduleId, "Booked");
        }

        return new InvoiceDTO(invoice);
    }
}
