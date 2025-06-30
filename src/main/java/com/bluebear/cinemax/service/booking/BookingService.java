package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.BookingRequestDTO;
import com.bluebear.cinemax.dto.BookingResultDTO;
import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.dto.SepayWebhookDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.enumtype.PaymentMethod;
import com.bluebear.cinemax.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InvoiceRepository invoiceRepository;
    private final DetailSeatRepository detailSeatRepository;
    private final DetailFDRepository detailFDRepository;
    private final SeatRepository seatRepository;
    private final TheaterStockRepository theaterStockRepository;
    private final PromotionRepository promotionRepository;
    private final ScheduleRepository scheduleRepository;

    private final TransactionRepository transactionRepository;

    @Transactional
    public InvoiceDTO initiateBooking(BookingRequestDTO request) {
        try {
            List<Seat> seats = seatRepository.findAllById(request.getSelectedSeats());
            Promotion promotion = request.getPromotionId() != null ?
                    promotionRepository.findById(request.getPromotionId()).orElse(null) : null;

            // Tính tiền vé
            double totalTicketPrice = seats.stream().mapToDouble(Seat::getUnitPrice).sum();

            // Tính tiền đồ ăn
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

            // Tính giảm giá
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
            // Đọc lại chi tiết đơn hàng từ chuỗi JSON đã lưu
            BookingRequestDTO originalRequest = objectMapper.readValue(invoice.getBookingDetails(), BookingRequestDTO.class);

            // --- Bắt đầu logic đặt vé thực sự ---
            Schedule schedule = scheduleRepository.findById(originalRequest.getScheduleId())
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));
            List<Seat> seats = seatRepository.findAllById(originalRequest.getSelectedSeats());

            // Tạo chi tiết ghế
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

            // Tạo chi tiết đồ ăn và trừ kho
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
                        stockItem.setQuantity(stockItem.getQuantity() - quantity);
                        theaterStockRepository.save(stockItem);

                        double itemTotalPrice = stockItem.getPrice() * quantity;
                        Detail_FD detailFd = Detail_FD.builder()
                                .invoice(invoice)
                                .theaterStock(stockItem)
                                .quantity(quantity)
                                .totalPrice(itemTotalPrice)
                                .status(InvoiceStatus.Booked) // Hoặc Paid
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

            // Cập nhật trạng thái hóa đơn
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

        return BookingResultDTO.builder()
                .invoiceId(invoice.getInvoiceID())
                .bookingDate(invoice.getBookingDate().format(dateFormatter))
                .customerName(invoice.getGuestName())
                .customerPhone(invoice.getGuestPhone())
                .totalPrice(BigDecimal.valueOf(invoice.getTotalPrice()))
                .totalTicketPrice(BigDecimal.valueOf(totalTicketPrice))
                .totalFoodPrice(BigDecimal.valueOf(totalFoodPrice))
                .movieName(schedule.getMovie().getMovieName())
                .movieDuration(String.valueOf(schedule.getMovie().getDuration()))
                .scheduleTime(schedule.getStartTime().format(timeFormatter))
                .roomName(schedule.getRoom().getName())
                .seatPositions(seats.stream().map(Seat::getPosition).collect(Collectors.toList()))
                .foodItems(foodItems)
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
                    stockItem.setQuantity(stockItem.getQuantity() - quantity);
                    theaterStockRepository.save(stockItem);
                    double itemTotalPrice = stockItem.getPrice() * quantity;
                    Detail_FD detailFd = Detail_FD.builder()
                            .invoice(savedInvoice)
                            .theaterStock(stockItem)
                            .quantity(quantity)
                            .totalPrice(itemTotalPrice)
                            .status(InvoiceStatus.Booked)
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

}
