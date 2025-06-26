package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.BookingRequestDTO;
import com.bluebear.cinemax.dto.BookingResultDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
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

    private final InvoiceRepository invoiceRepository;
    private final DetailSeatRepository detailSeatRepository;
    private final DetailFDRepository detailFDRepository;
    private final SeatRepository seatRepository;
    private final TheaterStockRepository theaterStockRepository;
    private final PromotionRepository promotionRepository;
    private final ScheduleRepository scheduleRepository; // Giả sử bạn đã có repository này

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


}