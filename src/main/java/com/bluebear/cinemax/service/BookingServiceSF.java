package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.*;
import com.bluebear.cinemax.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BookingServiceSF {
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
    private Detail_FDRepository detailFDRepo;
    @Autowired
    private RoomRepository roomRepo;

    public List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId) {
        List<Seat> seats = seatRepo.findByRoomRoomID(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat : seats) {
            boolean booked = detailSeatRepo.existsBySeatSeatIDAndScheduleScheduleID(seat.getSeatID(), scheduleId);
            SeatDTO dto = new SeatDTO();
            dto.setSeatID(seat.getSeatID());
            dto.setPosition(seat.getPosition());
            dto.setSeatType(TypeOfSeat.valueOf(seat.getSeatType().name()));
            dto.setIsVIP(seat.getIsVIP());
            dto.setUnitPrice(seat.getUnitPrice());
            dto.setBooked(booked);
            seatDTOs.add(dto);
        }
        return seatDTOs;
    }

    /**
     * Xác thực mã khuyến mãi.
     * Trả về một Optional<PromotionDTO> nếu mã hợp lệ và còn hiệu lực.
     */
    public Optional<PromotionDTO> validatePromotionCode(String code) {
        return promotionRepo.findByPromotionCode(code)
                .filter(p -> p.getQuantity() > 0 && p.getEndTime().isAfter(LocalDateTime.now()))
                .map(this::toDTO);
    }

    public List<TheaterStockDTO> getAvailableCombos() {
        List<TheaterStock> activeCombos = theaterStockRepo.findByStatus(TheaterStock_Status.Active);
        return activeCombos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceDTO bookSeatsAndCombos(Integer scheduleId, Integer customerId, List<Integer> seatIds, String promotionCode, Map<Integer, Integer> selectedCombos) {
        // Lấy thông tin lịch chiếu và khách hàng
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch chiếu."));
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy khách hàng."));

        // Tính tổng tiền vé và kiểm tra ghế
        double seatTotal = 0.0;
        List<Seat> seats = seatRepo.findAllById(seatIds);
        for (Seat seat : seats) {
            if (detailSeatRepo.existsBySeatSeatIDAndScheduleScheduleID(seat.getSeatID(), scheduleId)) {
                throw new IllegalStateException("Một số ghế đã được đặt: " + seat.getPosition());
            }
            seatTotal += seat.getUnitPrice().doubleValue();
        }

        double finalTotal = seatTotal;
        double discount = 0.0;
        Promotion promo = null;

        // Áp dụng khuyến mãi nếu có
        if (promotionCode != null && !promotionCode.isEmpty()) {
            Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode)
                    .filter(p -> p.getQuantity() > 0 && p.getEndTime().isAfter(LocalDateTime.now()));

            if (promoOpt.isPresent()) {
                promo = promoOpt.get();
                discount = seatTotal * promo.getDiscount() / 100.0;
                finalTotal -= discount;
                promo.setQuantity(promo.getQuantity() - 1);
                promotionRepo.save(promo);
            } else {
                throw new IllegalStateException("Mã giảm giá không hợp lệ hoặc đã hết hạn.");
            }
        }

        // Xử lý combo (đồ ăn & nước uống)
        double comboTotal = 0.0;
        List<Detail_FD> detailFDs = new ArrayList<>();
        for (Map.Entry<Integer, Integer> comboEntry : selectedCombos.entrySet()) {
            TheaterStock stock = theaterStockRepo.findById(comboEntry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Combo không tồn tại: " + comboEntry.getKey()));

            if (stock.getQuantity() < comboEntry.getValue()) {
                throw new IllegalStateException("Không đủ hàng cho combo: " + stock.getItemName());
            }

            stock.setQuantity(stock.getQuantity() - comboEntry.getValue());
            theaterStockRepo.save(stock);

            double comboPrice = stock.getPrice().doubleValue() * 1000 * comboEntry.getValue();
            comboTotal += comboPrice;

            Detail_FD detailFD = new Detail_FD();
            detailFD.setTheaterStock(stock);
            detailFD.setQuantity(comboEntry.getValue());
            detailFD.setTotalPrice(comboPrice);
            detailFD.setStatus(InvoiceStatus.Booked);
            detailFDs.add(detailFD);
        }

        finalTotal += comboTotal;

        // Tạo và lưu hóa đơn
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setPromotion(promo);
        invoice.setBookingDate(LocalDateTime.now());
        // Giả sử trạng thái 'Paid' tồn tại và phù hợp cho một đơn đặt hàng thành công.
        invoice.setStatus(InvoiceStatus.Booked);
        invoice.setTotalPrice(finalTotal);
        invoice = invoiceRepo.save(invoice);

        // Lưu chi tiết các ghế đã đặt
        for (Seat seat : seats) {
            DetailSeat detailSeat = DetailSeat.builder()
                    .invoice(invoice)
                    .seat(seat)
                    .schedule(schedule)
                    .status(DetailSeat_Status.Booked) // Sử dụng enum thay vì chuỗi String
                    .build();
            detailSeatRepo.save(detailSeat);
        }

        // Lưu chi tiết combo với hóa đơn vừa tạo
        for (Detail_FD detailFD : detailFDs) {
            detailFD.setInvoice(invoice);
            detailFDRepo.save(detailFD);
        }

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
            response.put("finalPrice", totalAmount - discount);
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

        Optional<PromotionDTO> promotionDTOOpt = promotionCode != null ? validatePromotionCode(promotionCode) : Optional.empty();

        // Chuyển đổi sang DTO
        ScheduleDTO scheduleDTO = toDTO(schedule);
        RoomDTO roomDTO = toDTO(room);
        List<SeatDTO> seatDTOs = toSeatDTOList(selectedSeats);
        List<TheaterStockDTO> comboDTOs = toTheaterStockDTOList(combos, comboQuantities);
        PromotionDTO promotionDTO = promotionDTOOpt.orElse(null);

        // Tính toán giá
        double totalSeatPrice = seatDTOs.stream().mapToDouble(SeatDTO::getUnitPrice).sum();
        double totalComboPrice = comboDTOs.stream()
                .mapToDouble(combo -> combo.getUnitPrice() * comboQuantities.getOrDefault(combo.getTheaterStockId(), 0))
                .sum();
        double totalPrice = totalSeatPrice + totalComboPrice;
        double discountAmount = 0;
        if (promotionDTO != null) {
            discountAmount = totalSeatPrice * promotionDTO.getDiscount() / 100.0;
        }
        double finalPrice = totalPrice - discountAmount;

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
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    // --- DTO Converters ---

    public InvoiceDTO toDTO(Invoice invoice) {
        Double discount = (invoice.getPromotion() != null) ? invoice.getPromotion().getDiscount() : 0.0f;

        return InvoiceDTO.builder()
                .invoiceID(invoice.getInvoiceID())
                .customerID(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                .employeeID(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null)
                .promotionID(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null)
                .discount(discount)
                .bookingDate(invoice.getBookingDate())
                .totalPrice(invoice.getTotalPrice())
                .status(invoice.getStatus())
                .build();
    }

    public PromotionDTO toDTO(Promotion promotion) {
        if (promotion == null) return null;
        return PromotionDTO.builder()
                .promotionID(promotion.getPromotionID())
                .promotionCode(promotion.getPromotionCode())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .quantity(promotion.getQuantity())
                .status(promotion.getStatus())
                .discount(promotion.getDiscount())
                .build();
    }

    public TheaterStockDTO toDTO(TheaterStock theaterStock) {
        TheaterStockDTO dto = new TheaterStockDTO();
        dto.setTheaterStockId(theaterStock.getStockID());
        if (theaterStock.getTheater() != null) {
            dto.setTheater(toDTO(theaterStock.getTheater()));
        }
        dto.setFoodName(theaterStock.getItemName());
        dto.setQuantity(theaterStock.getQuantity());
        dto.setUnitPrice(theaterStock.getPrice() * 1000);
        dto.setImage(theaterStock.getImage());
        dto.setStatus(theaterStock.getStatus().name());
        return dto;
    }

    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatID());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? TypeOfSeat.valueOf(seat.getSeatType().name()) : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.getIsVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? Seat_Status.valueOf(seat.getStatus().name()) : null);
        return dto;
    }

    public List<SeatDTO> toSeatDTOList(List<Seat> seats) {
        return seats.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks, Map<Integer, Integer> quantities) {
        return stocks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private TheaterDTO toDTO(Theater theater) {
        if (theater == null) return null;
        // Chuyển đổi cơ bản để tránh các đối tượng lồng nhau phức tạp
        return TheaterDTO.builder()
                .theaterID(theater.getTheaterID())
                .theaterName(theater.getTheaterName())
                .address(theater.getAddress())
                .image(theater.getImage())
                .status(theater.getStatus())
                .build();
    }

    public RoomDTO toDTO(Room room) {
        return RoomDTO.builder()
                .roomID(room.getRoomID())
                .theaterID(room.getTheater().getTheaterID())
                .name(room.getName())
                .collumn(room.getCollumn())
                .row(room.getRow())
                .typeOfRoom(room.getTypeOfRoom())
                .status(room.getStatus())
                .seats(room.getSeats() != null ? toSeatDTOList(room.getSeats()) : null)
                .schedules(room.getSchedules() != null ? room.getSchedules().stream().map(this::toDTO).collect(Collectors.toList()) : null)
                .build();
    }

    public ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleID());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setStatus(schedule.getStatus());

        if (schedule.getMovie() != null) {
            MovieDTO movieDTO = new MovieDTO();
            movieDTO.setMovieID(schedule.getMovie().getMovieID());
            movieDTO.setMovieName(schedule.getMovie().getMovieName());
            dto.setMovieID(movieDTO.getMovieID());
        }

        if (schedule.getRoom() != null) {
            dto.setRoomID(schedule.getRoom().getRoomID());
        }

        return dto;
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

}