package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CashierService {

    // ====== MOVIE ======
    // 1. Phim theo rạp + ngày
    Page<MovieDTO> getPagedMovieByTheater(Integer theaterId, LocalDate date, Pageable pageable);

    // 2. Phim theo rạp + keyword + ngày
    Page<MovieDTO> searchPagedMoviesByTheaterAndKeyword(Integer theaterId, String keyword, LocalDate date, Pageable pageable);

    // 3. Phim theo rạp + thể loại + ngày
    Page<MovieDTO> getPagedMoviesByTheaterAndGenre(Integer theaterId, Integer genreId, LocalDate date, Pageable pageable);

    // 4. Phim theo rạp + thể loại + keyword + ngày
    Page<MovieDTO> searchPagedMoviesByTheaterAndGenreAndKeyword(Integer theaterId, Integer genreId, String keyword, LocalDate date, Pageable pageable);

    // ====== SCHEDULE ======
    List<ScheduleDTO> getSchedulesByMovieAndDate(Integer theaterId, Integer movieId, LocalDate date);

    // ====== ROOM ======
    List<RoomDTO> getRoomsByTheater(Integer theaterId);

    // ====== SEAT ======
    List<SeatDTO> getSeatsBySchedule(Integer scheduleId);

    List<SeatDTO> getAvailableSeatsBySchedule(Integer scheduleId);

    // Method mới: lấy ghế với thông tin booking chi tiết
    List<SeatDTO> getSeatsWithBookingDetails(Integer scheduleId);

    // ====== THEATER STOCK ======
    List<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId);

}