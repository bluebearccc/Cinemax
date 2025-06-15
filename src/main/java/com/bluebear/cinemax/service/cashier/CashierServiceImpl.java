package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.cashier.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashierServiceImpl implements CashierService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TheaterStockRepository theaterStockRepository;

    @Autowired
    private DetailSeatRepository detailSeatRepository;


    // ====== CONVERT TO DTO METHODS ======

    public MovieDTO toDTO(Movie movie) {
        List<GenreDTO> genres = movie.getMovieGenres().stream()
                .map(MovieGenre::getGenre)
                .map(g -> new GenreDTO(g.getGenreId(), g.getGenreName()))
                .collect(Collectors.toList());

        List<MovieFeedbackDTO> feedbacks = movie.getMovieFeedbacks().stream()
                .map(f -> new MovieFeedbackDTO(
                        f.getId(),
                        f.getCustomer().getFullName(),
                        f.getContent(),
                        f.getMovieRate()
                ))
                .collect(Collectors.toList());

        return new MovieDTO(
                movie.getMovieId(),
                movie.getMovieName(),
                movie.getDescription(),
                movie.getImage(),
                movie.getBanner(),
                movie.getStudio(),
                movie.getDuration(),
                movie.getTrailer(),
                movie.getMovieRate(),
                movie.getActor(),
                movie.getStartDate(),
                movie.getEndDate(),
                movie.getStatus().name(),
                genres,
                feedbacks
        );
    }

    private ScheduleDTO convertToScheduleDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());

        dto.setMovie(toDTO(schedule.getMovie()));
        dto.setRoom(convertToRoomDTO(schedule.getRoom()));

        dto.setStatus(schedule.getStatus().name());
        return dto;
    }

    private RoomDTO convertToRoomDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomId(room.getRoomId());
        roomDTO.setName(room.getName());
        roomDTO.setColumn(room.getColumn());
        roomDTO.setRow(room.getRow());
        roomDTO.setTypeOfRoom(room.getTypeOfRoom().name());
        roomDTO.setStatus(room.getStatus().name());
        return roomDTO;
    }

    private SeatDTO convertToSeatDTO(Seat seat) {
        SeatDTO seatDTO = new SeatDTO();
        seatDTO.setSeatId(seat.getSeatId());
        RoomDTO roomDTO = convertToRoomDTO(seat.getRoom());
        seatDTO.setRoom(roomDTO);
        seatDTO.setSeatType(seat.getSeatType().name());
        seatDTO.setPosition(seat.getPosition());
        seatDTO.setIsVIP(seat.getIsVIP());
        seatDTO.setUnitPrice(seat.getUnitPrice());
        seatDTO.setStatus(seat.getStatus().name());

        // Default booking status
        seatDTO.setIsBooked(false);
        seatDTO.setBookingStatus("AVAILABLE");
        seatDTO.setBookingId(null);

        return seatDTO;
    }

    private SeatDTO convertToSeatDTOWithBookingStatus(Seat seat, Integer scheduleId) {
        SeatDTO seatDTO = convertToSeatDTO(seat);

        // Check if this seat is booked for the specific schedule
        List<Integer> bookedSeatIds = detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId);

        if (bookedSeatIds.contains(seat.getSeatId())) {
            seatDTO.setIsBooked(true);
            seatDTO.setBookingStatus("CONFIRMED");

            // Get the actual booking details if needed
            List<DetailSeat> detailSeats = detailSeatRepository.findByScheduleId(scheduleId);
            detailSeats.stream()
                    .filter(ds -> ds.getSeat().getSeatId().equals(seat.getSeatId()))
                    .findFirst()
                    .ifPresent(ds -> {
                        seatDTO.setBookingId(ds.getInvoice().getInvoiceId());
                        // You can add more booking status logic here if needed
                    });
        } else {
            seatDTO.setIsBooked(false);
            seatDTO.setBookingStatus("AVAILABLE");
            seatDTO.setBookingId(null);
        }

        return seatDTO;
    }

    // Method to get seats with booking details
    @Override
    public List<SeatDTO> getSeatsWithBookingDetails(Integer scheduleId) {
        try {
            // Lấy thông tin schedule trước để có roomId
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            Integer roomId = schedule.getRoom().getRoomId();

            // Lấy tất cả ghế trong phòng
            List<Seat> seats = seatRepository.findByRoomIdAndStatus(roomId, Seat.SeatStatus.Active);

            // Lấy danh sách ghế đã được book cho schedule này
            List<Integer> bookedSeatIds = detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId);
            Set<Integer> bookedSeatIdSet = new HashSet<>(bookedSeatIds);

            return seats.stream()
                    .map(seat -> {
                        SeatDTO seatDTO = convertToSeatDTO(seat);

                        // Set booking status
                        if (bookedSeatIdSet.contains(seat.getSeatId())) {
                            seatDTO.setIsBooked(true);
                            seatDTO.setBookingStatus("CONFIRMED");

                            // Tìm booking ID nếu cần
                            detailSeatRepository.findByScheduleIdAndSeatId(scheduleId, seat.getSeatId())
                                    .ifPresent(ds -> seatDTO.setBookingId(ds.getInvoice().getInvoiceId()));
                        } else {
                            seatDTO.setIsBooked(false);
                            seatDTO.setBookingStatus("AVAILABLE");
                            seatDTO.setBookingId(null);
                        }

                        return seatDTO;
                    })
                    .sorted((s1, s2) -> s1.getPosition().compareTo(s2.getPosition()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting seats with booking details for scheduleId " + scheduleId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of throwing exception
        }
    }

    private TheaterDTO convertToTheaterDTO(Theater theater) {
        if (theater == null) {
            return null;
        }

        return new TheaterDTO(
                theater.getTheaterId(),
                theater.getTheaterName(),
                theater.getAddress(),
                theater.getImage(),
                theater.getRoomQuantity(),
                theater.getStatus() != null ? theater.getStatus().name() : null
        );
    }

    private TheaterStockDTO convertToTheaterStockDTO(TheaterStock theaterStock) {
        if (theaterStock == null) {
            return null;
        }

        TheaterDTO theaterDTO = convertToTheaterDTO(theaterStock.getTheater());

        return new TheaterStockDTO(
                theaterStock.getTheaterStockId(),
                theaterDTO,
                theaterStock.getFoodName(),
                theaterStock.getQuantity(),
                theaterStock.getUnitPrice(),
                theaterStock.getImage(),
                theaterStock.getStatus() != null ? theaterStock.getStatus().name() : null
        );
    }

    // ====== MOVIE SERVICES ======

    @Override
    public Page<MovieDTO> getPagedMovieByTheater(Integer theaterId, LocalDate date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndDate(
                theaterId, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, date, pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> searchPagedMoviesByTheaterAndKeyword(Integer theaterId, String keyword, LocalDate date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndKeywordAndDate(
                theaterId, keyword, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, date, pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> getPagedMoviesByTheaterAndGenre(Integer theaterId, Integer genreId, LocalDate date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndGenreIdAndDate(
                theaterId, genreId, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, date, pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> searchPagedMoviesByTheaterAndGenreAndKeyword(Integer theaterId, Integer genreId, String keyword, LocalDate date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndGenreIdAndKeywordAndDate(
                theaterId, genreId, keyword, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, date, pageable
        );
        return movies.map(this::toDTO);
    }

    // ====== SCHEDULE SERVICES ======

    @Override
    public List<ScheduleDTO> getSchedulesByMovieAndDate(Integer theaterId, Integer movieId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByMovieAndDateAndTheater(
                movieId, Schedule.ScheduleStatus.Active, date, theaterId
        );
        return schedules.stream()
                .map(this::convertToScheduleDTO)
                .collect(Collectors.toList());
    }

    // ====== ROOM SERVICES ======

    @Override
    public List<RoomDTO> getRoomsByTheater(Integer theaterId) {
        List<Room> rooms = roomRepository.findByTheaterIdAndStatus(
                theaterId, Room.RoomStatus.Active
        );
        return rooms.stream()
                .map(this::convertToRoomDTO)
                .collect(Collectors.toList());
    }

    // ====== SEAT SERVICES ======

    @Override
    public List<SeatDTO> getSeatsBySchedule(Integer scheduleId) {
        List<Seat> seats = seatRepository.findSeatsWithBookingsByScheduleIdAndStatus(
                scheduleId, Seat.SeatStatus.Active
        );
        return seats.stream()
                .map(seat -> convertToSeatDTOWithBookingStatus(seat, scheduleId))
                .collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableSeatsBySchedule(Integer scheduleId) {
        // Get all active seats for the schedule
        List<Seat> allSeats = seatRepository.findSeatsWithBookingsByScheduleIdAndStatus(
                scheduleId, Seat.SeatStatus.Active
        );

        // Get booked seat IDs for this schedule
        Set<Integer> bookedSeatIds = Set.copyOf(detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId));

        // Filter out booked seats
        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getSeatId()))
                .collect(Collectors.toList());

        return availableSeats.stream()
                .map(this::convertToSeatDTO)
                .collect(Collectors.toList());
    }

    // ====== THEATER STOCK SERVICES ======

    @Override
    public List<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId) {
        List<TheaterStock> theaterStocks = theaterStockRepository.findByTheaterIdAndStatus(
                theaterId, TheaterStock.StockStatus.Active
        );
        return theaterStocks.stream()
                .map(this::convertToTheaterStockDTO)
                .collect(Collectors.toList());
    }
}