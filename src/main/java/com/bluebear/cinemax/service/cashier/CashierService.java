package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;

import com.bluebear.cinemax.entity.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface CashierService {
    // ====== MOVIE ======
    // 1. Phim theo rạp + ngày
    Page<MovieDTO> getPagedMovieByTheater(Integer theaterId, LocalDateTime date, Pageable pageable);

    // 2. Phim theo rạp + keyword + ngày
    Page<MovieDTO> searchPagedMoviesByTheaterAndKeyword(Integer theaterId, String keyword, LocalDateTime date, Pageable pageable);

    // 3. Phim theo rạp + thể loại + ngày
    Page<MovieDTO> getPagedMoviesByTheaterAndGenre(Integer theaterId, Integer genreId, LocalDateTime date, Pageable pageable);

    // 4. Phim theo rạp + thể loại + keyword + ngày
    Page<MovieDTO> searchPagedMoviesByTheaterAndGenreAndKeyword(Integer theaterId, Integer genreId, String keyword, LocalDateTime date, Pageable pageable);

    // ====== SCHEDULE ======
    List<ScheduleDTO> getSchedulesByMovieAndDate(Integer theaterId, Integer movieId, LocalDateTime startTime, LocalDateTime endTime);

    List<SeatDTO> getAvailableSeatsBySchedule(Integer scheduleId);

    List<SeatDTO> getSeatsWithBookingDetails(Integer scheduleId);

    // ====== THEATER STOCK ======
    Page<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId, Pageable pageable);

    SeatDTO convertToSeatDTO(Seat seat);

    BookingResponseDTO createBooking(BookingRequestDTO bookingRequest);

    BookingResponseDTO getBookingById(String bookingId);
}