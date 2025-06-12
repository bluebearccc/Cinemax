package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.entity.Movie;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CashierService {

    // Movie methods
    List<MovieDTO> getMovieAvailable(Movie.MovieStatus status, LocalDate currentDate);
    MovieDTO getMovieById(Integer movieId);

    // Schedule methods
    List<ScheduleDTO> getAllSchedulesByMovieIdAndDate(Integer movieId, LocalDate date);

    // Seat methods
    List<SeatDTO> getAvailableSeatsByScheduleId(Integer scheduleId);

    // Customer methods
//    List<CustomerDTO> searchCustomers(String term);
//    CustomerDTO getCustomerByPhone(String phone);
//    CustomerDTO createCustomer(CustomerDTO customerDTO);

    // Food & Drink methods
//    List<TheaterStockDTO> getFoodMenuByTheaterId(Integer theaterId);
//    TheaterStockDTO getFoodById(Integer foodId);
//
//    // Promotion methods
//    PromotionDTO getPromotionByCode(String promotionCode);
//    Float calculateDiscount(String promotionCode, Float totalAmount);
//
//    // Booking methods
//    Integer createBooking(MovieDTO movie, Map<String, Object> schedule,
//                          String[] seats, Map<String, Object> customerInfo,
//                          Map<String, Object> priceBreakdown);
//
//    // Invoice methods
//    InvoiceDTO getInvoiceById(Integer invoiceId);
//
//    // Theater methods
//    List<TheaterDTO> getAllTheaters();
}