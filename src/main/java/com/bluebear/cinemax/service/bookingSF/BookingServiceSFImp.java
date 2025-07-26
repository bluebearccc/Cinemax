package com.bluebear.cinemax.service.bookingSF;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.*;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.email.EmailService;
import com.bluebear.cinemax.service.seat.SeatService;
import com.bluebear.cinemax.service.theaterstock.TheaterStockService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceSFImp implements BookingServiceSF {
    @Autowired
    private TransactionRepository transactionRepo;
    @Autowired
    private TheaterStockService theaterStockService;
    @Autowired
    private SeatService seatService;
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
    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public void finalizeBooking(Integer invoiceId) {
        Invoice invoiceEntity = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoiceEntity.getStatus() == InvoiceStatus.Booked) {
            log.info("💡 Hóa đơn {} đã được thanh toán trước đó.", invoiceId);
            return;
        }

        // Cập nhật trạng thái hóa đơn
        invoiceEntity.setStatus(InvoiceStatus.Booked);
        invoiceRepo.save(invoiceEntity);

        // Cập nhật trạng thái DetailSeat (cách làm đơn giản và an toàn hơn)
        List<DetailSeat> detailSeatEntities = detailSeatRepo.findByInvoiceInvoiceID(invoiceId);
        for (DetailSeat entity : detailSeatEntities) {
            entity.setStatus(DetailSeat_Status.Booked);
            detailSeatRepo.save(entity);
        }

        // Cập nhật trạng thái combo (Detail_FD)
        List<Detail_FD> comboEntities = detailFDRepo.findByInvoiceInvoiceID(invoiceId);
        for (Detail_FD entity : comboEntities) {
            entity.setStatus(DetailFD_Status.Booked);
            detailFDRepo.save(entity);
        }

        // Gửi email xác nhận vé
        Customer customer = invoiceEntity.getCustomer();
        if (customer != null && customer.getAccount() != null && customer.getAccount().getEmail() != null) {
            String customerEmail = customer.getAccount().getEmail();
            try {
                Map<String, Object> variables = new HashMap<>();
                // Dùng hàm reconstruct đã được sửa lỗi để lấy dữ liệu an toàn
                BookingPreviewDTO previewForEmail = reconstructBookingPreview(invoiceId);
                variables.put("invoice", previewForEmail);
                variables.put("customerName", customer.getFullName());

                // SỬA LỖI: Gửi đến email thực của khách hàng
                emailService.sendTicketHtmlTemplate(
                        customerEmail,
                        "Cinemax - Xác nhận đặt vé thành công #" + invoiceId,
                        variables
                );
                log.info("📧 Đã gửi email xác nhận đến: {}", customerEmail);
            } catch (Exception e) {
                log.error("❌ Gửi email thất bại cho hóa đơn #{}: {}", invoiceId, e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ Không tìm thấy thông tin email của khách hàng cho hóa đơn #{}", invoiceId);
        }

        log.info("✅ finalizeBooking hoàn tất cho invoice #{}", invoiceId);
    }

    /**
     * SỬA LỖI: Hàm này được làm cho an toàn hơn để tránh lỗi IndexOutOfBoundsException.
     * Nó sẽ kiểm tra xem hóa đơn có ghế liên quan hay không trước khi xử lý.
     */
    @Override
    public BookingPreviewDTO reconstructBookingPreview(Integer invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy hóa đơn #" + invoiceId));

        List<DetailSeat> detailSeats = detailSeatRepo.findByInvoiceInvoiceID(invoiceId);
        List<Detail_FD> detailCombos = detailFDRepo.findByInvoiceInvoiceID(invoiceId);

        // KIỂM TRA AN TOÀN: Nếu không có ghế nào được tìm thấy, trả về một preview rỗng để tránh crash
        if (detailSeats.isEmpty()) {
            log.warn("⚠️ Hóa đơn #{} không có ghế nào được liên kết khi cố gắng tái tạo preview.", invoiceId);
            // Trả về một DTO rỗng hoặc với thông tin cơ bản để trang không bị lỗi
            return new BookingPreviewDTO(
                    new ScheduleDTO(), new RoomDTO(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyMap(),
                    invoice.getTotalPrice(), invoice.getTotalPrice(), toDTO(invoice.getPromotion())
            );
        }

        List<Seat> seats = detailSeats.stream().map(DetailSeat::getSeat).collect(Collectors.toList());
        Schedule schedule = detailSeats.get(0).getSchedule();
        Room room = schedule.getRoom();
        Promotion promotion = invoice.getPromotion();

        Map<Integer, Integer> comboQuantities = detailCombos.stream()
                .collect(Collectors.toMap(fd -> fd.getTheaterStock().getStockID(), Detail_FD::getQuantity));
        List<TheaterStock> combos = detailCombos.stream().map(Detail_FD::getTheaterStock).collect(Collectors.toList());

        return new BookingPreviewDTO(
                toDTO(schedule),
                toDTO(room),
                seatService.toSeatDTOList(seats),
                toTheaterStockDTOList(combos),
                comboQuantities,
                invoice.getTotalPrice() + invoice.getDiscount(),
                invoice.getTotalPrice(),
                promotion != null ? toDTO(promotion) : null
        );
    }

    // ... (Các hàm còn lại giữ nguyên)

    @Override
    public InvoiceDTO getInvoiceById(Integer invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn #" + invoiceId));
        return toDTO(invoice);
    }

    public List<TheaterStockDTO> getAvailableCombos(Integer roomId) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu #" + roomId));
        Integer theaterId = room.getTheater().getTheaterID();

        List<TheaterStock> activeCombos = theaterStockRepo.findByTheater_TheaterIDAndStatus(theaterId, TheaterStock_Status.Active);
        return activeCombos.stream()
                .map(theaterStockService::convertToDTO)
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

        Customer customer = customerRepo.findById(1)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy khách hàng."));
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setPromotion(appliedPromotion);
        invoice.setDiscount(discount);
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setStatus(InvoiceStatus.Unpaid);
        invoice.setTotalPrice(0.0);
        invoice = invoiceRepo.save(invoice);

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

            double comboPrice = stock.getPrice().doubleValue() * quantity;
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

    public BookingPreviewDTO prepareBookingPreview(Integer scheduleId, Integer roomId,
                                                   List<Integer> seatIds, String promotionCode,
                                                   Map<Integer, Integer> comboQuantities) {
        Schedule schedule = scheduleRepo.findById(scheduleId).orElseThrow();
        Room room = roomRepo.findById(roomId).orElseThrow();
        List<Seat> selectedSeats = seatRepo.findAllById(seatIds);
        List<TheaterStock> combos = theaterStockRepo.findAllById(comboQuantities.keySet());
        Promotion promotion = promotionCode != null ? promotionRepo.findByPromotionCode(promotionCode).orElse(null) : null;

        ScheduleDTO scheduleDTO = toDTO(schedule);
        RoomDTO roomDTO = toDTO(room);
        List<SeatDTO> seatDTOs = seatService.toSeatDTOList(selectedSeats);
        List<TheaterStockDTO> comboDTOs = toTheaterStockDTOList(combos);
        PromotionDTO promotionDTO = promotion != null ? toDTO(promotion) : null;

        double totalSeatPrice = seatDTOs.stream().mapToDouble(SeatDTO::getUnitPrice).sum();
        double totalComboPrice = combos.stream()
                .mapToDouble(combo -> combo.getPrice().doubleValue() * comboQuantities.getOrDefault(combo.getStockID(), 0))
                .sum();
        double totalPrice = totalSeatPrice + (totalComboPrice);
        double discount = (promotionDTO != null && promotionDTO.isValid()) ? promotionDTO.getDiscount() / 100.0 : 0.0;
        double finalPrice = totalPrice * (1 - discount);

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

    public List<TheaterStockDTO> toTheaterStockDTOList(List<TheaterStock> stocks) {
        return stocks.stream().map(theaterStockService::convertToDTO).toList();
    }
    public List<TheaterStockDTO> filterCombosByKeyword(String keyword,Integer roomId) {
        List<TheaterStockDTO> combos = getAvailableCombos(roomId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return combos;
        }
        return combos.stream()
                .filter(c -> c.getFoodName().toLowerCase().contains(keyword.trim().toLowerCase()))
                .toList();
    }
    @Override
    @Transactional
    public void cancelInvoice(Integer invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn #" + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.Unpaid) {
            List<Detail_FD> detailFDs = detailFDRepo.findByInvoiceInvoiceID(invoiceId);
            for (Detail_FD fd : detailFDs) {
                TheaterStock stock = fd.getTheaterStock();
                stock.setQuantity(stock.getQuantity() + fd.getQuantity());
                theaterStockRepo.save(stock);
            }
            detailFDRepo.deleteAll(detailFDs);

            List<DetailSeat> detailSeats = detailSeatRepo.findByInvoiceInvoiceID(invoiceId);
            detailSeatRepo.deleteAll(detailSeats);

            invoiceRepo.delete(invoice);

            log.info("🗑️ Đã xoá toàn bộ dữ liệu liên quan đến invoice #{}", invoiceId);
        } else {
            log.warn("⚠️ Invoice #{} không ở trạng thái UNPAID, không thể huỷ và xoá.", invoiceId);
        }
    }

    @Override
    @Transactional
    public void saveTransactionFromWebhook(SepayWebhookDTO payload) {
        log.info("💬 Nhận webhook từ Sepay: {}", payload);

        try {
            Transaction tx = mapToTransaction(payload);
            transactionRepo.save(tx);
            log.info("✅ Giao dịch Sepay đã lưu vào DB với reference: {}", tx.getReferenceNumber());
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu giao dịch Sepay: {}", e.getMessage(), e);
        }
    }

    public InvoiceDTO createTemporaryInvoice(BookingPreviewDTO previewData, Integer customerId) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customerRepo.findById(customerId).orElse(null));
        invoice.setStatus(InvoiceStatus.Unpaid);
        invoice.setBookingDate(LocalDateTime.now());
        invoice.setTotalPrice(previewData.getFinalPrice());
        invoice.setPromotion(previewData.getPromotion() != null ?
                promotionRepo.findById(previewData.getPromotion().getPromotionID()).orElse(null) : null);
        invoice.setDiscount(previewData.getTotalPrice() - previewData.getFinalPrice());
        invoice = invoiceRepo.save(invoice);

        Schedule schedule = scheduleRepo.findById(previewData.getSchedule().getScheduleID()).orElseThrow();
        for (SeatDTO seatDTO : previewData.getSelectedSeats()) {
            Seat seat = seatRepo.findById(seatDTO.getSeatID()).orElseThrow();
            DetailSeat detailSeat = new DetailSeat();
            detailSeat.setInvoice(invoice);
            detailSeat.setSchedule(schedule);
            detailSeat.setSeat(seat);
            detailSeat.setStatus(DetailSeat_Status.Unpaid);
            detailSeatRepo.save(detailSeat);
        }

        return toDTO(invoice);
    }

    public Map<String, Object> getTicketTemplateData(Integer invoiceId) {
        InvoiceDTO invoice = getInvoiceById(invoiceId);
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceCode", "DH" + invoice.getInvoiceID());
        data.put("guestName", invoice.getGuestName());
        data.put("guestEmail", invoice.getGuestEmail());
        data.put("guestPhone", invoice.getGuestPhone());
        data.put("bookingDate", invoice.getBookingDate());
        List<String> seatList = invoice.getDetailSeats()
                .stream()
                .map(detailSeatDTO -> {
                    SeatDTO seat = seatService.getSeatById(detailSeatDTO.getSeatID());
                    return seat.getPosition();
                })
                .collect(Collectors.toList());
        data.put("seatList", seatList);
        List<String> comboList = invoice.getDetail_FDDTO()
                .stream()
                .map(fd -> fd.getItemName() + " x" + fd.getQuantity())
                .toList();
        data.put("comboList", comboList);
        data.put("totalPrice", invoice.getTotalPrice());
        data.put("discount", invoice.getDiscount());
        return data;
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
        if (promotion == null) return null;
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

    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatID());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType() : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.getIsVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? seat.getStatus() : null);
        return dto;
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
                .seats(room.getSeats() != null ? room.getSeats().stream().map(this::toDTO).toList() : null)
                .schedules(room.getSchedules() != null ? room.getSchedules().stream().map(schedule -> new ScheduleDTO()).toList() : null)
                .build();
    }

    public ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleID());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMovieID(schedule.getMovie() != null ? schedule.getMovie().getMovieID() : null);
        dto.setRoomID(schedule.getRoom() != null ? schedule.getRoom().getRoomID() : null);
        dto.setStatus(schedule.getStatus());
        dto.setMovieName(schedule.getMovie() != null ? schedule.getMovie().getMovieName() : null);

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

    private Transaction mapToTransaction(SepayWebhookDTO dto) {
        Transaction tx = new Transaction();
        tx.setGateway(dto.getGateway());
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            tx.setTransactionDate(LocalDateTime.parse(dto.getTransactionDate(), formatter));
        } catch (Exception e) {
            log.warn("⚠ Không thể parse transactionDate '{}'", dto.getTransactionDate(), e);
        }
        tx.setAccountNumber(dto.getAccountNumber());
        tx.setSubAccount(dto.getSubAccount());
        tx.setAmountIn(dto.getTransferAmount() != null && dto.getTransferAmount() > 0 ? dto.getTransferAmount() : 0.0);
        tx.setAmountOut(0.0);
        tx.setAccumulated(dto.getAccumulated());
        tx.setCode(dto.getCode());
        tx.setTransactionContent(dto.getContent());
        tx.setReferenceNumber(dto.getReferenceCode());
        tx.setBody(dto.toString());
        return tx;
    }
}
