package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.Invoice_Status;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BookingService {
        @Autowired
        private SeatRepository seatRepo;
        @Autowired
        private DetailSeatRepository detailSeatRepo;
        @Autowired
        private PromotionRepository promotionRepo;
        @Autowired
        private InvoiceRepository invoiceRepo;
        @Autowired
        private ScheduleRepository scheduleRepo;
        @Autowired
        private CustomerRepository customerRepo;
        @Autowired
        private TheaterStockRepository theaterStockRepo;
        @Autowired
        private DetailFDRepository detailFDRepo;
        @Autowired
        private RoomRepository roomRepo;

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
                .filter(Promotion::isValid)
                .map(this::toDTO);
    }
    public List<TheaterStockDTO> getAvailableCombos() {
        List<TheaterStock> activeCombos = theaterStockRepo.findByStatus(Theater_Status.Active);
        return activeCombos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
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

                double comboTotalPrice = stock.getUnitPrice().doubleValue() * 1000 * combo.getValue();
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

            return toDTO(invoice);
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
    public BookingPreviewDTO prepareBookingPreview(Integer scheduleId, Integer roomId,
                                                   List<Integer> seatIds, String promotionCode,
                                                   Map<Integer, Integer> comboQuantities) {
        // Lấy dữ liệu từ Repository
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow();
        Room room = roomRepo.findById(roomId).orElseThrow();
        List<Seat> selectedSeats = seatRepo.findAllById(seatIds);
        List<TheaterStock> combos = theaterStockRepo.findAllById(comboQuantities.keySet());
        Promotion promotion = promotionCode != null ? promotionRepo.findByPromotionCode(promotionCode).orElse(null) : null;

        // Chuyển đổi sang DTO
        ScheduleDTO scheduleDTO = toDTO(schedule);
        RoomDTO roomDTO = toDTO(room);
        List<SeatDTO> seatDTOs = toSeatDTOList(selectedSeats);
        List<TheaterStockDTO> comboDTOs = toTheaterStockDTOList(combos);
        PromotionDTO promotionDTO = promotion != null ? toDTO(promotion) : null;

        // Tính toán giá
        double totalSeatPrice = seatDTOs.stream().mapToDouble(SeatDTO::getUnitPrice).sum();
        double totalComboPrice = combos.stream()
                .mapToDouble(combo -> combo.getUnitPrice().doubleValue() * comboQuantities.getOrDefault(combo.getTheaterStockID(), 0))
                .sum();
        double totalPrice = totalSeatPrice + totalComboPrice;
        double discount = (promotionDTO != null && promotionDTO.isValid()) ? promotionDTO.getDiscount() / 100.0 : 0.0;
        double finalPrice = totalPrice * (1 - discount);

        // Trả về BookingPreviewDTO
        return new BookingPreviewDTO(scheduleDTO, roomDTO, seatDTOs, comboDTOs, comboQuantities, totalPrice, finalPrice, promotionDTO);
    }
    public Map<Integer, Integer> extractComboQuantities(Map<String, String> allParams) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("comboQuantities[")) {
                String idStr = key.substring(16, key.length() - 1);
                try {
                    Integer comboId = Integer.parseInt(idStr);
                    Integer quantity = Integer.parseInt(entry.getValue());
                    if (quantity > 0) {
                        result.put(comboId, quantity);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return result;
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


    public InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .customerId(invoice.getCustomer() != null ? invoice.getCustomer().getID() : null)
                .employeeId(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null)
                .promotionId(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null)
                .discount(invoice.getDiscount())
                .bookingDate(invoice.getBookingDate())
                .totalprice(invoice.getTotalPrice())
                .status(invoice.getStatus())
                .build();
    }

    public Invoice toEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(dto.getInvoiceId());
        invoice.setDiscount(dto.getDiscount());
        invoice.setBookingDate(dto.getBookingDate());
        invoice.setTotalPrice(dto.getTotalprice());
        invoice.setStatus(dto.getStatus());

        if (dto.getCustomerId() != null) {
            customerRepo.findById(dto.getCustomerId()).ifPresent(invoice::setCustomer);
        }
        if (dto.getPromotionId() != null) {
            promotionRepo.findById(dto.getPromotionId()).ifPresent(invoice::setPromotion);
        }
        return invoice;
    }
    public PromotionDTO toDTO(Promotion promotion) {
        return new PromotionDTO(
                promotion.getPromotionID(),
                promotion.getPromotionCode(),
                promotion.getDiscount(),
                promotion.getStartTime(),
                promotion.getEndTime(),
                promotion.getQuantity(),
                promotion.getStatus()
        );
    }
    public TheaterStockDTO toDTO(TheaterStock theaterStock) {
        TheaterStockDTO dto = new TheaterStockDTO();
        dto.setTheaterStockID(theaterStock.getTheaterStockID());
        dto.setTheater(theaterStock.getTheater());
        dto.setFoodName(theaterStock.getFoodName());
        dto.setQuantity(theaterStock.getQuantity());
        dto.setUnitPrice(theaterStock.getUnitPrice()*1000);
        dto.setImage(theaterStock.getImage());
        dto.setStatus(theaterStock.getStatus());
        return dto;
    }
    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatId());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? TypeOfSeat.valueOf(seat.getSeatType()) : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.isVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? Seat_Status.valueOf(seat.getStatus()) : null);
        return dto;
    }
    public List<SeatDTO> toSeatDTOList(List<Seat> seats) {
        return seats.stream().map(this::toDTO).toList();
    }

    public List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks) {
        return stocks.stream().map(this::toDTO).toList();
    }
    public RoomDTO toDTO(Room room) {
        RoomDTO roomDTO = RoomDTO.builder()
                .roomID(room.getRoomID())
                .theaterID(room.getTheater().getTheaterId())
                .name(room.getName())
                .collumn(room.getCollumn())
                .row(room.getRow())
                .typeOfRoom(room.getTypeOfRoom())
                .status(room.getStatus())
                .seats(room.getSeats() != null ? room.getSeats().stream().map(this::toDTO).toList() : null)
                .schedules(room.getSchedules() != null ? room.getSchedules().stream().map(schedule -> {
                    // Implement ScheduleDTO conversion logic here
                    return new ScheduleDTO(); // Replace with actual conversion logic
                }).toList() : null)
                .build();

        return roomDTO;
    }
    public ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMovieID(schedule.getMovie() != null ? schedule.getMovie().getMovieID() : null);
        dto.setRoomID(schedule.getRoom() != null ? schedule.getRoom().getRoomID() : null);
        dto.setStatus(schedule.getStatus());

        if (schedule.getMovie() != null) {
            MovieDTO movieDTO = new MovieDTO();
            movieDTO.setMovieID(schedule.getMovie().getMovieID());
            movieDTO.setMovieName(schedule.getMovie().getMovieName());
            dto.setMovie(movieDTO);
        }

        if (schedule.getRoom() != null) {
            RoomDTO roomDTO = new RoomDTO();
            roomDTO.setRoomID(schedule.getRoom().getRoomID());
            roomDTO.setName(schedule.getRoom().getName());
            dto.setRoom(roomDTO);
        }

        return dto;
    }
}

