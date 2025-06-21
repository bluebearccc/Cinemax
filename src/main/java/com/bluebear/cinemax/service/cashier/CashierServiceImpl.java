package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.cashier.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CashierServiceImpl implements CashierService {

    private final LocalDateTime currentDate = LocalDateTime.now();
    LocalDateTime sevenDaysFromToday = currentDate.plusDays(7);

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TheaterStockRepository theaterStockRepository;

    @Autowired
    private DetailSeatRepository detailSeatRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private DetailFDRepository detailFDRepository;

    @Autowired
    private AccountRepository accountRepository;

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

    public SeatDTO convertToSeatDTO(Seat seat) {
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

    private BookingResponseDTO mapInvoiceToResponseDTO(Invoice invoice, Double totalTicketPrice, Double totalFoodPrice) {
        Schedule schedule = invoice.getDetailSeats().getFirst().getSchedule();
        Movie movie = schedule.getMovie();

        List<String> seatPositions = invoice.getDetailSeats().stream()
                .map(detailSeat -> detailSeat.getSeat().getPosition())
                .collect(Collectors.toList());

        List<BookingResponseDTO.FoodItemDetail> foodItems = invoice.getDetailFDs().stream()
                .map(detailFD -> BookingResponseDTO.FoodItemDetail.builder()
                        .name(detailFD.getTheaterStock().getFoodName())
                        .quantity(detailFD.getQuantity())
                        .unitPrice(detailFD.getTheaterStock().getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return BookingResponseDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .bookingDate(invoice.getBookingDate().format(dateFormatter))
                .totalPrice(invoice.getTotalPrice())
                .customerName(invoice.getCustomer().getFullName())
                .customerPhone(invoice.getCustomer().getPhone())
                .movieName(movie.getMovieName())
                .movieDuration(movie.getDuration())
                .scheduleTime(schedule.getStartTime().format(timeFormatter))
                .roomName(schedule.getRoom().getName())
                .seatPositions(seatPositions)
                .totalTicketPrice(totalTicketPrice)
                .foodItems(foodItems)
                .totalFoodPrice(totalFoodPrice)
                .build();
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

    //==================================SERVICE IMPLEMENT====================================================================
    @Override
    public List<SeatDTO> getSeatsWithBookingDetails(Integer scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new RuntimeException("Schedule not found"));

            Integer roomId = schedule.getRoom().getRoomId();

            List<Seat> seats = seatRepository.findByRoomIdAndStatus(roomId, Seat.SeatStatus.Active);

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

    @Override
    public Page<MovieDTO> getPagedMovieByTheater(Integer theaterId, LocalDateTime date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndDate(
                theaterId, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, currentDate, sevenDaysFromToday,pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> searchPagedMoviesByTheaterAndKeyword(Integer theaterId, String keyword, LocalDateTime date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndKeywordAndDate(
                theaterId, keyword, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, currentDate, sevenDaysFromToday,pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> getPagedMoviesByTheaterAndGenre(Integer theaterId, Integer genreId, LocalDateTime date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndGenreIdAndDate(
                theaterId, genreId, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, currentDate, sevenDaysFromToday,pageable
        );
        return movies.map(this::toDTO);
    }

    @Override
    public Page<MovieDTO> searchPagedMoviesByTheaterAndGenreAndKeyword(Integer theaterId, Integer genreId, String keyword, LocalDateTime date, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByTheaterIdAndGenreIdAndKeywordAndDate(
                theaterId, genreId, keyword, Movie.MovieStatus.Active, Theater.TheaterStatus.Active, currentDate, sevenDaysFromToday,pageable
        );
        return movies.map(this::toDTO);
    }

    // ====== SCHEDULE SERVICES ======

    @Override
    public List<ScheduleDTO> getSchedulesByMovieAndDate(Integer theaterId, Integer movieId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Schedule> schedules = scheduleRepository.findByMovieAndDateRangeAndTheater(
                movieId, Schedule.ScheduleStatus.Active, currentDate, sevenDaysFromToday, theaterId
        );
        return schedules.stream()
                .map(this::convertToScheduleDTO)
                .collect(Collectors.toList());
    }

    // ====== SEAT SERVICE ======
    @Override
    public List<SeatDTO> getAvailableSeatsBySchedule(Integer scheduleId) {
        List<Seat> allSeats = seatRepository.findSeatsWithBookingsByScheduleIdAndStatus(
                scheduleId, Seat.SeatStatus.Active
        );

        Set<Integer> bookedSeatIds = Set.copyOf(detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId));

        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getSeatId()))
                .collect(Collectors.toList());

        return availableSeats.stream()
                .map(this::convertToSeatDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TheaterStockDTO> getAvailableTheaterStockByTheater(Integer theaterId, Pageable pageable) {
        Page<TheaterStock> theaterStocks = theaterStockRepository.findByTheaterIdAndStatus(
                theaterId, TheaterStock.StockStatus.Active, pageable
        );
        return  theaterStocks.map(this::convertToTheaterStockDTO);
    }

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO request) {
        Optional<Customer> optionalCustomer = customerRepository.findByPhone(request.getCustomerPhone());
        Customer customer;

        if (optionalCustomer.isPresent()) {
            customer = optionalCustomer.get();
        } else {

            Account newAccount = new Account();
            newAccount.setEmail(request.getCustomerEmail());
            newAccount.setRole(Account.Role.Customer);
            newAccount.setStatus(Account.AccountStatus.Active);
            newAccount.setPassword("123");

            Account savedAccount = accountRepository.save(newAccount);

            Customer newCustomer = new Customer();
            newCustomer.setAccount(savedAccount);
            newCustomer.setFullName(request.getCustomerName());
            newCustomer.setPhone(request.getCustomerPhone());
            newCustomer.setPoint(0);

            customer = customerRepository.save(newCustomer); // Lưu customer mới và gán lại biến
        }


        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found with ID: " + request.getScheduleId()));

        if (schedule.getStatus() != Schedule.ScheduleStatus.Active) {
            throw new RuntimeException("Schedule is not active");
        }


        List<Seat> seats = seatRepository.findAllById(request.getSelectedSeatIds());
        Double totalTicketPrice = seats.stream().map(Seat::getUnitPrice).reduce(0.0, Double::sum);

        float discount = 0.0f;
        Promotion promotion = null;
        if (request.getPromotionId() != null) {
            promotion = promotionRepository.findPromotionByPromotionIdAndStatus(request.getPromotionId(), Promotion.PromotionStatus.Available);
            if (promotion != null) {
                discount = promotion.getDiscount();
            }
        }

        Double totalFoodPrice = 0.0;
        List<DetailFD> detailFds = new ArrayList<>();

        if (request.getFoodQuantities() != null && !request.getFoodQuantities().isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : request.getFoodQuantities().entrySet()) {
                Integer foodId = entry.getKey();
                Integer quantity = entry.getValue();

                if (quantity > 0) {
                    Optional<TheaterStock> optionalTheaterStock = theaterStockRepository.findById(foodId);
                    if (optionalTheaterStock.isPresent()) {
                        TheaterStock theaterStock = optionalTheaterStock.get();
                        if (theaterStock.getStatus() == TheaterStock.StockStatus.Active) {
                            totalFoodPrice += theaterStock.getUnitPrice() * quantity;

                            // Tạo chi tiết đồ ăn cho TỪNG món
                            DetailFD detailFD = new DetailFD();
                            detailFD.setQuantity(quantity);
                            detailFD.setTheaterStock(theaterStock);
                            // detailFD.setInvoice(null); // Sẽ gán sau
                            detailFds.add(detailFD);
                        }
                    }
                }
            }
        }

        Double totalPrice = (totalFoodPrice + totalTicketPrice) * (1 - discount);


        Invoice invoice = Invoice.builder()
                .customer(customer)
                .promotion(promotion)
                .discount(discount * 100)
                .bookingDate(LocalDateTime.now())
                .totalPrice(totalPrice)
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        List<DetailSeat> detailSeats = new ArrayList<>();
        for (Seat seat : seats) {
            DetailSeat detailSeat = new DetailSeat();
            detailSeat.setInvoice(savedInvoice);
            detailSeat.setSeat(seat);
            detailSeat.setSchedule(schedule);
            detailSeats.add(detailSeat);
        }
        detailSeatRepository.saveAll(detailSeats);
        savedInvoice.setDetailSeats(detailSeats);

        if (!detailFds.isEmpty()) {
            for (DetailFD detailFD : detailFds) {
                detailFD.setInvoice(savedInvoice);
            }
            detailFDRepository.saveAll(detailFds);
            savedInvoice.setDetailFDs(detailFds);
        }

        return mapInvoiceToResponseDTO(savedInvoice, totalTicketPrice, totalFoodPrice);
    }

    @Override
    public BookingResponseDTO getBookingById(String bookingId) {
        return null;
    }
}