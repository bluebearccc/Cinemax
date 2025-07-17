package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.*;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class BookingServiceSFImp implements BookingSerivceSF {
    @Autowired
    private EmailService emailService;
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





    public Optional<PromotionDTO> validatePromotionCode(String code) {
        return promotionRepo.findByPromotionCode(code)
                .filter(Promotion::isValid)
                .map(this::toDTO);
    }
    public List<TheaterStockDTO> getAvailableCombos() {
        List<TheaterStock> activeCombos = theaterStockRepo.findByStatus(TheaterStock_Status.Active);
        return activeCombos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public InvoiceDTO bookSeatsAndCombos(Integer scheduleId, List<Integer> seatIds,
                                         String promotionCode, Map<Integer, Integer> selectedCombos) {
        double seatTotal = 0.0;
        List<Seat> seats = seatRepo.findAllById(seatIds);

        for (Seat seat : seats) {
            List<DetailSeat> related = detailSeatRepo.findBySeatSeatIDAndScheduleScheduleIDAndStatusIn(
                    seat.getSeatID(), scheduleId,
                    List.of(DetailSeat_Status.Unpaid, DetailSeat_Status.Booked)
            );

            for (DetailSeat ds : related) {
                Invoice invoice = ds.getInvoice();
                if (ds.getStatus() == DetailSeat_Status.Booked) {
                    throw new IllegalStateException("Ghế " + seat.getPosition() + " đã được đặt.");
                }

                if (ds.getStatus() == DetailSeat_Status.Unpaid &&
                        invoice != null &&
                        invoice.getBookingDate() != null &&
                        invoice.getBookingDate().isAfter(LocalDateTime.now().minusMinutes(15))) {
                    throw new IllegalStateException("Ghế " + seat.getPosition() + " đang được giữ.");
                }
            }

            seatTotal += seat.getUnitPrice().doubleValue();
        }

        // Handle promotion
        Promotion appliedPromotion = null;
        double discount = 0.0;
        if (promotionCode != null && !promotionCode.isBlank()) {
            Optional<Promotion> promoOpt = promotionRepo.findByPromotionCode(promotionCode);
            if (promoOpt.isEmpty()) {
                throw new IllegalStateException("Mã giảm giá không tồn tại.");
            }

            Promotion promo = promoOpt.get();
            if (promo.getEndTime().isBefore(LocalDateTime.now()) || promo.getQuantity() <= 0) {
                throw new IllegalStateException("Mã giảm giá đã hết hạn hoặc không còn khả dụng.");
            }

            discount = seatTotal * promo.getDiscount() / 100.0;
            seatTotal -= discount;

            promo.setQuantity(promo.getQuantity() - 1);
            promotionRepo.save(promo);

            appliedPromotion = promo;
        }

        // Prepare Invoice
        Customer customer = customerRepo.findById(1)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy khách hàng."));
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setPromotion(appliedPromotion);
        invoice.setDiscount(discount);
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.Unpaid);
        invoice.setTotalPrice(0.0); // tạm thời, cập nhật sau
        invoice = invoiceRepo.save(invoice);

        // Save detail seats
        Schedule schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lịch chiếu."));
        for (Seat seat : seats) {
            DetailSeat detailSeat = new DetailSeat();
            detailSeat.setInvoice(invoice);
            detailSeat.setSeat(seat);
            detailSeat.setSchedule(schedule);
            detailSeat.setStatus(DetailSeat_Status.Unpaid);
            detailSeatRepo.save(detailSeat);
        }
        // Handle combos
        double comboTotal = 0.0;
        for (Map.Entry<Integer, Integer> entry : selectedCombos.entrySet()) {
            TheaterStock stock = theaterStockRepo.findById(entry.getKey())
                    .orElseThrow(() -> new IllegalStateException("Combo không tồn tại: " + entry.getKey()));
            int quantity = entry.getValue();

            if (stock.getQuantity() < quantity) {
                throw new IllegalStateException("Không đủ hàng cho combo: " + stock.getItemName());
            }

            stock.setQuantity(stock.getQuantity() - quantity);
            theaterStockRepo.save(stock);

            double comboPrice = stock.getPrice().doubleValue() * 1000 * quantity;
            comboTotal += comboPrice;

            Detail_FD detailFD = new Detail_FD();
            detailFD.setInvoice(invoice);
            detailFD.setTheaterStock(stock);
            detailFD.setQuantity(quantity);
            detailFD.setTotalPrice(comboPrice);
            detailFD.setStatus(DetailFD_Status.Booked);
            detailFDRepo.save(detailFD);
        }

        double finalTotal = seatTotal + comboTotal;
        invoice.setTotalPrice(finalTotal);
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
                .mapToDouble(combo -> combo.getPrice().doubleValue() * comboQuantities.getOrDefault(combo.getStockID(), 0))
                .sum();
        double totalPrice = totalSeatPrice + (totalComboPrice*1000);
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

    public List<SeatDTO> toSeatDTOList(List<Seat> seats) {
        return seats.stream().map(this::toDTO).toList();
    }

    public List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks) {
        return stocks.stream().map(this::toDTO).toList();
    }

    public InvoiceDTO toDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .invoiceID(invoice.getInvoiceID())
                .customerID(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)
                .employeeID(invoice.getEmployee() != null ? invoice.getEmployee().getId() : null)
                .promotionID(invoice.getPromotion() != null ? invoice.getPromotion().getPromotionID() : null)
                .discount(invoice.getDiscount())
                .bookingDate(invoice.getBookingDate())
                .totalPrice(invoice.getTotalPrice())
                .status(invoice.getStatus())
                .build();
    }

    public Invoice toEntity(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceID(dto.getInvoiceID());
        invoice.setDiscount(dto.getDiscount());
        invoice.setBookingDate(dto.getBookingDate());
        invoice.setTotalPrice(dto.getTotalPrice());
        invoice.setStatus(dto.getStatus());

        if (dto.getCustomerID() != null) {
            customerRepo.findById(dto.getCustomerID()).ifPresent(invoice::setCustomer);
        }
        if (dto.getPromotionID() != null) {
            promotionRepo.findById(dto.getPromotionID()).ifPresent(invoice::setPromotion);
        }
        return invoice;
    }

    public PromotionDTO toDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionID(promotion.getPromotionID());
        dto.setPromotionCode(promotion.getPromotionCode());
        dto.setDiscount(promotion.getDiscount());
        dto.setStartTime(promotion.getStartTime());
        dto.setEndTime(promotion.getEndTime());
        dto.setQuantity(promotion.getQuantity());
        dto.setStatus(promotion.getStatus());
        return dto;
    }

    public TheaterStockDTO toDTO(TheaterStock theaterStock) {
        TheaterStockDTO dto = new TheaterStockDTO();
        dto.setTheaterStockId(theaterStock.getStockID());
        dto.setTheater(toEntity(theaterStock.getTheater()));
        dto.setFoodName(theaterStock.getItemName());
        dto.setQuantity(theaterStock.getQuantity());
        dto.setUnitPrice(theaterStock.getPrice()*1000);
        dto.setImage(theaterStock.getImage());
        dto.setStatus(theaterStock.getStatus() != null ? theaterStock.getStatus().name() : null);
        return dto;
    }

    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatID());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType() : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.isVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? seat.getStatus() : null);
        return dto;
    }

    public RoomDTO toDTO(Room room) {
        RoomDTO roomDTO = RoomDTO.builder()
                .roomID(room.getRoomID())
                .theaterID(room.getTheater().getTheaterID())
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
        dto.setScheduleID(schedule.getScheduleID());
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
    public DetailSeatDTO toDTO(DetailSeat detailSeat) {
        DetailSeatDTO dto = new DetailSeatDTO();
        dto.setId(detailSeat.getId());
        dto.setInvoiceID(detailSeat.getInvoice() != null ? detailSeat.getInvoice().getInvoiceID() : null);
        dto.setSeatID(detailSeat.getSeat() != null ? detailSeat.getSeat().getSeatID() : null);
        dto.setScheduleID(detailSeat.getSchedule() != null ? detailSeat.getSchedule().getScheduleID() : null);
        dto.setStatus(detailSeat.getStatus());
        return dto;
    }
    private TheaterDTO toEntity(Theater theater) {
        if (theater == null) {
            return null;
        }

        return TheaterDTO.builder()
                .theaterID(theater.getTheaterID())
                .theaterName(theater.getTheaterName())
                .address(theater.getAddress())
                .image(theater.getImage())
                .roomQuantity(theater.getRoomQuantity())
                .status(theater.getStatus())
                .build();
    }

}