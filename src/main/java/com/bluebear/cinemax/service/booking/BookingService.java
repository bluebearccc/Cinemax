package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailFD_Status;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.enumtype.PaymentMethod;
import com.bluebear.cinemax.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private DetailFDRepository detailFDRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private TheaterStockRepository theaterStockRepository;
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public InvoiceDTO initiateBooking(BookingRequestDTO request) {
        try {
            List<Seat> seats = seatRepository.findAllById(request.getSelectedSeats());
            Promotion promotion = request.getPromotionId() != null ?
                    promotionRepository.findById(request.getPromotionId()).orElse(null) : null;

            double totalTicketPrice = seats.stream().mapToDouble(Seat::getUnitPrice).sum();

            double totalFoodPrice = 0.0;
            if (request.getFoodQuantities() != null) {
                for (Map.Entry<Integer, Integer> entry : request.getFoodQuantities().entrySet()) {
                    if (entry.getValue() > 0) {
                        TheaterStock stockItem = theaterStockRepository.findById(entry.getKey())
                                .orElseThrow(() -> new RuntimeException("Stock item not found"));
                        totalFoodPrice += stockItem.getPrice() * entry.getValue();
                    }
                }
            }

            double discount = promotion != null ? (totalTicketPrice * promotion.getDiscount() / 100) : 0.0;
            double finalTotalPrice = totalTicketPrice + totalFoodPrice - discount;

            String bookingDetailsJson = objectMapper.writeValueAsString(request);

            Invoice invoice = Invoice.builder()
                    .guestName(request.getCustomerName())
                    .guestPhone(request.getCustomerPhone())
                    .guestEmail(request.getCustomerEmail())
                    .bookingDate(LocalDateTime.now())
                    .promotion(promotion)
                    .status(InvoiceStatus.Unpaid)
                    .totalPrice(finalTotalPrice)
                    .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                    .bookingDetails(bookingDetailsJson)
                    .build();

            Invoice savedInvoice = invoiceRepository.save(invoice);

            return InvoiceDTO.builder()
                    .invoiceID(savedInvoice.getInvoiceID())
                    .totalPrice(savedInvoice.getTotalPrice())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Could not initiate booking", e);
        }
    }

    @Transactional
    public BookingResultDTO finalizeBooking(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.Unpaid) {
            throw new IllegalStateException("Invoice is not in Unpaid state.");
        }

        try {
            BookingRequestDTO originalRequest = objectMapper.readValue(invoice.getBookingDetails(), BookingRequestDTO.class);

            Schedule schedule = scheduleRepository.findById(originalRequest.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            List<Seat> seats = seatRepository.findAllById(originalRequest.getSelectedSeats());

            double totalTicketPrice = 0.0;
            for (Seat seat : seats) {
                DetailSeat detailSeat = DetailSeat.builder()
                        .invoice(invoice)
                        .schedule(schedule)
                        .seat(seat)
                        .status(DetailSeat_Status.Booked)
                        .build();
                detailSeatRepository.save(detailSeat);
                totalTicketPrice += seat.getUnitPrice();
            }

            double totalFoodPrice = 0.0;
            List<BookingResultDTO.FoodItemDTO> foodItemsDTO = new ArrayList<>();
            if (originalRequest.getFoodQuantities() != null) {
                for (Map.Entry<Integer, Integer> entry : originalRequest.getFoodQuantities().entrySet()) {
                    if (entry.getValue() > 0) {
                        TheaterStock stockItem = theaterStockRepository.findById(entry.getKey())
                                .orElseThrow(() -> new RuntimeException("Stock item not found"));
                        int quantity = entry.getValue();
                        if (stockItem.getQuantity() < quantity) {
                            throw new RuntimeException("Not enough stock for " + stockItem.getItemName());
                        }
                        // **ĐÃ XÓA LOGIC TRỪ TỒN KHO TẠI ĐÂY**
                        // Việc trừ tồn kho sẽ do trigger của database xử lý khi Detail_FD được lưu.

                        double itemTotalPrice = stockItem.getPrice() * quantity;
                        Detail_FD detailFd = Detail_FD.builder()
                                .invoice(invoice)
                                .theaterStock(stockItem)
                                .quantity(quantity)
                                .totalPrice(itemTotalPrice)
                                .status(DetailFD_Status.Booked)
                                .build();
                        detailFDRepository.save(detailFd);
                        totalFoodPrice += itemTotalPrice;

                        foodItemsDTO.add(BookingResultDTO.FoodItemDTO.builder()
                                .name(stockItem.getItemName())
                                .quantity(quantity)
                                .unitPrice(BigDecimal.valueOf(stockItem.getPrice()))
                                .build());
                    }
                }
            }

            invoice.setStatus(InvoiceStatus.Booked);
            invoiceRepository.save(invoice);

            return buildBookingResult(invoice, schedule, seats, foodItemsDTO, totalTicketPrice, totalFoodPrice);
        } catch (Exception e) {
            throw new RuntimeException("Could not finalize booking", e);
        }
    }

    @Transactional
    public void saveTransactionFromWebhook(SepayWebhookDTO dto) {
        Transaction transaction = new Transaction();
        transaction.setGateway(dto.getGateway());
        transaction.setTransactionDate(LocalDateTime.parse(dto.getTransactionDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        transaction.setAccountNumber(dto.getAccountNumber());
        transaction.setSubAccount(dto.getSubAccount());
        transaction.setTransactionContent(dto.getContent());
        transaction.setCode(dto.getCode());
        transaction.setReferenceNumber(dto.getReferenceCode());
        transaction.setBody(dto.getDescription());
        transaction.setAccumulated(dto.getAccumulated());

        if ("in".equalsIgnoreCase(dto.getTransferType())) {
            transaction.setAmountIn(dto.getTransferAmount());
            transaction.setAmountOut(0.0);
        } else {
            transaction.setAmountOut(dto.getTransferAmount());
            transaction.setAmountIn(0.0);
        }

        transactionRepository.save(transaction);
    }

    private BookingResultDTO buildBookingResult(Invoice invoice, Schedule schedule, List<Seat> seats, List<BookingResultDTO.FoodItemDTO> foodItems, double totalTicketPrice, double totalFoodPrice) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        double subTotalPrice = totalTicketPrice + totalFoodPrice;
        double discountAmount = 0.0;
        String promotionName = null;
        if (invoice.getPromotion() != null) {
            discountAmount = subTotalPrice * invoice.getPromotion().getDiscount() / 100;
            promotionName = invoice.getPromotion().getPromotionCode();
        }
        double unitTicketPrice = seats.isEmpty() ? 0 : seats.get(0).getUnitPrice();


        return BookingResultDTO.builder()
                .invoiceId(invoice.getInvoiceID())
                .bookingDate(invoice.getBookingDate())
                .customerName(invoice.getGuestName())
                .customerPhone(invoice.getGuestPhone())
                .totalPrice(invoice.getTotalPrice())
                .totalTicketPrice(totalTicketPrice)
                .totalFoodPrice(totalFoodPrice)
                .movieName(schedule.getMovie().getMovieName())
                .movieDuration(String.valueOf(schedule.getMovie().getDuration()))
                .scheduleTime(schedule.getStartTime().format(timeFormatter))
                .roomName(schedule.getRoom().getName())
                .seatPositions(seats.stream().map(Seat::getPosition).collect(Collectors.toList()))
                .foodItems(foodItems)
                .subTotalPrice(subTotalPrice)
                .promotionName(promotionName)
                .discountAmount(discountAmount)
                .unitTicketPrice(unitTicketPrice)
                .build();
    }

    public BookingResultDTO getBookingResult(Integer invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        List<DetailSeat> detailSeats = detailSeatRepository.findByInvoice(invoice);
        List<Detail_FD> detailFDs = detailFDRepository.findByInvoice(invoice);

        List<Seat> seats = detailSeats.stream().map(DetailSeat::getSeat).collect(Collectors.toList());
        Schedule schedule = detailSeats.get(0).getSchedule();

        double totalTicketPrice = seats.stream().mapToDouble(Seat::getUnitPrice).sum();
        double totalFoodPrice = detailFDs.stream().mapToDouble(Detail_FD::getTotalPrice).sum();

        List<BookingResultDTO.FoodItemDTO> foodItems = detailFDs.stream().map(dfd ->
                BookingResultDTO.FoodItemDTO.builder()
                        .name(dfd.getTheaterStock().getItemName())
                        .quantity(dfd.getQuantity())
                        .unitPrice(BigDecimal.valueOf(dfd.getTheaterStock().getPrice()))
                        .build()
        ).collect(Collectors.toList());

        return buildBookingResult(invoice, schedule, seats, foodItems, totalTicketPrice, totalFoodPrice);
    }

    @Transactional
    public BookingResultDTO createBooking(BookingRequestDTO request) {
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        List<Seat> seats = seatRepository.findAllById(request.getSelectedSeats());
        Promotion promotion = request.getPromotionId() != null ?
                promotionRepository.findById(request.getPromotionId()).orElse(null) : null;

        Invoice invoice = Invoice.builder()
                .guestName(request.getCustomerName())
                .guestPhone(request.getCustomerPhone())
                .guestEmail(request.getCustomerEmail())
                .bookingDate(LocalDateTime.now())
                .promotion(promotion)
                .status(InvoiceStatus.Booked)
                .totalPrice(0.0)
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        double totalTicketPrice = 0.0;
        for (Seat seat : seats) {
            DetailSeat detailSeat = DetailSeat.builder()
                    .invoice(savedInvoice)
                    .schedule(schedule)
                    .seat(seat)
                    .status(DetailSeat_Status.Booked)
                    .build();
            detailSeatRepository.save(detailSeat);
            totalTicketPrice += seat.getUnitPrice();
        }

        double totalFoodPrice = 0.0;
        List<BookingResultDTO.FoodItemDTO> foodItemsDTO = new ArrayList<>();
        if (request.getFoodQuantities() != null) {
            for (Map.Entry<Integer, Integer> entry : request.getFoodQuantities().entrySet()) {
                if (entry.getValue() > 0) {
                    TheaterStock stockItem = theaterStockRepository.findById(entry.getKey())
                            .orElseThrow(() -> new RuntimeException("Stock item not found"));
                    int quantity = entry.getValue();
                    if (stockItem.getQuantity() < quantity) {
                        throw new RuntimeException("Not enough stock for " + stockItem.getItemName());
                    }

                    double itemTotalPrice = stockItem.getPrice() * quantity;
                    Detail_FD detailFd = Detail_FD.builder()
                            .invoice(savedInvoice)
                            .theaterStock(stockItem)
                            .quantity(quantity)
                            .totalPrice(itemTotalPrice)
                            .status(DetailFD_Status.Booked)
                            .build();
                    detailFDRepository.save(detailFd);
                    totalFoodPrice += itemTotalPrice;

                    foodItemsDTO.add(BookingResultDTO.FoodItemDTO.builder()
                            .name(stockItem.getItemName())
                            .quantity(quantity)
                            .unitPrice(BigDecimal.valueOf(stockItem.getPrice()))
                            .build());
                }
            }
        }

        double discount = promotion != null ? (totalTicketPrice * promotion.getDiscount() / 100) : 0.0;
        double finalTotalPrice = totalTicketPrice + totalFoodPrice - discount;
        savedInvoice.setTotalPrice(finalTotalPrice);
        invoiceRepository.save(savedInvoice);

        return buildBookingResult(savedInvoice, schedule, seats, foodItemsDTO, totalTicketPrice, totalFoodPrice);
    }


    public CheckinDTO performCheckIn(Integer invoiceId) {
        LocalDateTime now = LocalDateTime.now();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.CHECKED_IN) {
            throw new IllegalStateException("Vé này đã được check-in trước đó.");
        }

        if (invoice.getStatus() != InvoiceStatus.Booked && invoice.getStatus() != InvoiceStatus.Unpaid) {
            throw new IllegalStateException("Vé không hợp lệ để check-in (chưa thanh toán hoặc đã hủy).");
        }

        // Optional: Kiểm tra thời gian check-in so với suất chiế
        if (now.isBefore(invoice.getBookingDate()) || now.isEqual(invoice.getBookingDate())) {

        }
        else {

        }
        // Ví dụ: chỉ cho phép check-in trong khoảng 1 giờ trước và sau giờ chiếu
        // ...

        // Cập nhật trạng thái
        invoice.setStatus(InvoiceStatus.CHECKED_IN);
        // Optional: Ghi nhận thời gian check-in
        // invoice.setCheckInTime(LocalDateTime.now());
        invoiceRepository.save(invoice);

        // Trả về kết quả để hiển thị cho nhân viên
        BookingResultDTO bookingResult = getBookingResult(invoiceId); // Tái sử dụng hàm đã có
        return new CheckinDTO("success", "Check-in thành công!", bookingResult);
    }
}