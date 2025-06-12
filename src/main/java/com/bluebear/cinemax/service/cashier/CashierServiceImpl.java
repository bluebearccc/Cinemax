package com.bluebear.cinemax.service.cashier;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.cashier.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CashierServiceImpl implements CashierService {

    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    @Autowired
    public CashierServiceImpl(MovieRepository movieRepository, ScheduleRepository scheduleRepository, SeatRepository seatRepository) {
        this.movieRepository = movieRepository;
        this.scheduleRepository = scheduleRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public List<MovieDTO> getMovieAvailable(Movie.MovieStatus status, LocalDate currentDate) {
        List<Movie> movies = movieRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                status, currentDate, currentDate);
        return movies.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public MovieDTO getMovieById(Integer movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        return (movie != null) ? convertToDto(movie) : null;
    }

    @Override
    public List<ScheduleDTO> getAllSchedulesByMovieIdAndDate(Integer movieId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findByMovieAndDate(
                movieId,
                Schedule.ScheduleStatus.Active,
                date
        );
        return schedules.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getAvailableSeatsByScheduleId(Integer scheduleId) {
        try {
            Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
            if (schedule == null) {
                return List.of();
            }
            Integer roomId = schedule.getRoom().getRoomId();
            List<Seat> availableSeats = seatRepository.findAvailableSeatsForSchedule(
                    roomId,
                    scheduleId,
                    Seat.SeatStatus.Active
            );
            return availableSeats.stream().map(this::convertToDto).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

//    @Override
//    public List<CustomerDTO> searchCustomers(String term) {
//        List<Customer> customers = customerRepository.findByFullNameContainingOrPhoneContaining(term, term);
//        return customers.stream().map(this::convertToDto).collect(Collectors.toList());
//    }

//    @Override
//    public CustomerDTO getCustomerByPhone(String phone) {
//        Customer customer = customerRepository.findByPhone(phone).get();
//        return customer != null ? convertToDto(customer) : null;
//    }
//
//    @Override
//    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
//        Customer customer = new Customer();
//        customer.setFullName(customerDTO.getFullName());
//        customer.setPhone(customerDTO.getPhone());
//        customer.setPoint(0);
//
//        // Create account if email provided
//        if (customerDTO.getAccountId() != null && customerDTO.getAccountId().getEmail() != null) {
//            Account account = new Account();
//            account.setEmail(customerDTO.getAccountId().getEmail());
//            account.setPassword("default123"); // Should be hashed
//            account.setRole(Account.Role.Customer);
//            account.setStatus(Account.AccountStatus.Active);
//            account = accountRepository.save(account);
//            customer.setAccount(account);
//        }
//
//        customer = customerRepository.save(customer);
//        return convertToDto(customer);
//    }

//    @Override
//    public List<TheaterStockDTO> getFoodMenuByTheaterId(Integer theaterId) {
//        List<TheaterStock> foodItems = theaterStockRepository.findByTheaterIdAndStatus(
//                theaterId, TheaterStock.StockStatus.Active);
//        return foodItems.stream().map(this::convertToDto).collect(Collectors.toList());
//    }
//
//    @Override
//    public TheaterStockDTO getFoodById(Integer foodId) {
//        TheaterStock food = theaterStockRepository.findById(foodId).orElse(null);
//        return food != null ? convertToDto(food) : null;
//    }
//
//    @Override
//    public PromotionDTO getPromotionByCode(String promotionCode) {
//        Promotion promotion = promotionRepository.findByPromotionCodeAndStatus(
//                promotionCode, Promotion.PromotionStatus.Available).get();
//        return promotion != null ? convertToDto(promotion) : null;
//    }
//
//    @Override
//    public Float calculateDiscount(String promotionCode, Float totalAmount) {
//        if (promotionCode == null || promotionCode.isEmpty()) {
//            return 0f;
//        }
//
//        PromotionDTO promotion = getPromotionByCode(promotionCode);
//        if (promotion != null &&
//                promotion.getStartTime().isBefore(LocalDateTime.now()) &&
//                promotion.getEndTime().isAfter(LocalDateTime.now()) &&
//                promotion.getQuantity() > 0) {
//            return promotion.getDiscount().floatValue();
//        }
//        return 0f;
//    }
//
//    @Override
//    @Transactional
//    public Integer createBooking(MovieDTO movie, Map<String, Object> schedule,
//                                 String[] seats, Map<String, Object> customerInfo,
//                                 Map<String, Object> priceBreakdown) {
//        try {
//            // Find or create customer
//            Customer customer = null;
//            String customerPhone = (String) customerInfo.get("phone");
//            customer = customerRepository.findByPhone(customerPhone).get();
//
//            if (customer == null || (Boolean) customerInfo.get("isNewCustomer")) {
//                customer = new Customer();
//                customer.setFullName((String) customerInfo.get("name"));
//                customer.setPhone(customerPhone);
//                customer.setPoint(0);
//
//                // Create account if email provided
//                String email = (String) customerInfo.get("email");
//                if (email != null && !email.isEmpty()) {
//                    Account account = new Account();
//                    account.setEmail(email);
//                    account.setPassword("default123"); // Should be hashed
//                    account.setRole(Account.Role.Customer);
//                    account.setStatus(Account.AccountStatus.Active);
//                    account = accountRepository.save(account);
//                    customer.setAccount(account);
//                }
//                customer = customerRepository.save(customer);
//            }
//
//            // Get employee (cashier) - assuming default employee ID 1
//            Employee employee = employeeRepository.findById(1).orElse(null);
//            if (employee == null) {
//                throw new RuntimeException("Employee not found");
//            }
//
//            // Get promotion if applied
//            Promotion promotion = null;
//            String promotionId = (String) customerInfo.get("promotionId");
//            if (promotionId != null && !promotionId.isEmpty()) {
//                promotion = promotionRepository.findById(Integer.parseInt(promotionId)).orElse(null);
//            }
//
//            // Create invoice
//            Invoice invoice = new Invoice();
//            invoice.setCustomer(customer);
//            invoice.setEmployee(employee);
//            invoice.setPromotion(promotion);
//            invoice.setDiscount(promotion != null ? promotion.getDiscount().floatValue() : 0f);
//            invoice.setBookingDate(LocalDateTime.now());
//
//            BigDecimal totalPrice = BigDecimal.valueOf(((Number) priceBreakdown.get("total")).doubleValue());
//            invoice.setTotalPrice(totalPrice);
//
//            invoice = invoiceRepository.save(invoice);
//
//            // Create food details if any
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> foodItems = (List<Map<String, Object>>) priceBreakdown.get("foodItems");
//            if (foodItems != null && !foodItems.isEmpty()) {
//                for (Map<String, Object> foodItem : foodItems) {
//                    // Find food by name (simplified - in real app should use ID)
//                    String foodName = (String) foodItem.get("name");
//                    TheaterStock theaterStock = theaterStockRepository.findByFoodName(foodName);
//
//                    if (theaterStock != null) {
//                        DetailFD detailFD = new DetailFD();
//                        detailFD.setInvoice(invoice);
//                        detailFD.setTheaterStock(theaterStock);
//                        detailFD.setQuantity((Integer) foodItem.get("quantity"));
//                        detailFD.setTotalPrice(BigDecimal.valueOf(
//                                ((Number) foodItem.get("totalPrice")).doubleValue()));
//
//                        detailFDRepository.save(detailFD);
//
//                        // Update stock quantity
//                        theaterStock.setQuantity(theaterStock.getQuantity() - detailFD.getQuantity());
//                        theaterStockRepository.save(theaterStock);
//                    }
//                }
//            }
//
//            // Update promotion quantity if used
//            if (promotion != null) {
//                promotion.setQuantity(promotion.getQuantity() - 1);
//                if (promotion.getQuantity() <= 0) {
//                    promotion.setStatus(Promotion.PromotionStatus.Expired);
//                }
//                promotionRepository.save(promotion);
//            }
//
//            return invoice.getInvoiceId();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to create booking", e);
//        }
//    }
//
//    @Override
//    public InvoiceDTO getInvoiceById(Integer invoiceId) {
//        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
//        return invoice != null ? convertToDto(invoice) : null;
//    }
//
//    @Override
//    public List<TheaterDTO> getAllTheaters() {
//        List<Theater> theaters = theaterRepository.findAll();
//        return theaters.stream().map(this::convertToDto).collect(Collectors.toList());
//    }

    // Converter methods
    private MovieDTO convertToDto(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setMovieName(movie.getMovieName());
        dto.setDescription(movie.getDescription());
        dto.setImage(movie.getImage());
        dto.setBanner(movie.getBanner());
        dto.setStudio(movie.getStudio());
        dto.setDuration(movie.getDuration());
        dto.setTrailer(movie.getTrailer());
        dto.setMovieRate(movie.getMovieRate());
        dto.setActor(movie.getActor());
        dto.setStartDate(movie.getStartDate());
        dto.setEndDate(movie.getEndDate());
        dto.setStatus(movie.getStatus().name());
        return dto;
    }

    private CustomerDTO convertToDto(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());
        dto.setPoint(customer.getPoint());
        if (customer.getAccount() != null) {
            dto.setAccountId(convertToDto(customer.getAccount()));
        }
        return dto;
    }

    private AccountDTO convertToDto(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setAccountId(account.getAccountId());
        dto.setEmail(account.getEmail());
        dto.setRole(account.getRole().name());
        dto.setStatus(account.getStatus().name());
        return dto;
    }

    private TheaterStockDTO convertToDto(TheaterStock stock) {
        TheaterStockDTO dto = new TheaterStockDTO();
        dto.setTheaterStockId(stock.getTheaterStockId());
        dto.setFoodName(stock.getFoodName());
        dto.setQuantity(stock.getQuantity());
        dto.setUnitPrice(stock.getUnitPrice());
        dto.setImage(stock.getImage());
        dto.setStatus(stock.getStatus().name());
        if (stock.getTheater() != null) {
            dto.setTheater(convertToDto(stock.getTheater()));
        }
        return dto;
    }

    private TheaterDTO convertToDto(Theater theater) {
        TheaterDTO dto = new TheaterDTO();

        return dto;
    }

    private PromotionDTO convertToDto(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setPromotionCode(promotion.getPromotionCode());
        dto.setDiscount(promotion.getDiscount());
        dto.setStartTime(promotion.getStartTime());
        dto.setEndTime(promotion.getEndTime());
        dto.setQuantity(promotion.getQuantity());
        dto.setStatus(promotion.getStatus().name());
        return dto;
    }

    private InvoiceDTO convertToDto(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(invoice.getInvoiceId());
        dto.setDiscount(invoice.getDiscount());
        dto.setBookingDate(invoice.getBookingDate());
        dto.setTotalPrice(invoice.getTotalPrice());

        if (invoice.getCustomer() != null) {
            dto.setCustomerId(convertToDto(invoice.getCustomer()));
        }
        if (invoice.getEmployee() != null) {
            dto.setEmployeeId(convertToDto(invoice.getEmployee()));
        }
        if (invoice.getPromotion() != null) {
            dto.setPromotionId(convertToDto(invoice.getPromotion()));
        }
        return dto;
    }

    private EmployeeDTO convertToDto(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFullName(employee.getFullName());
        dto.setAccount(convertToDto(employee.getAccount()));
        dto.setPosition(employee.getPosition().name());
        return dto;
    }

    private ScheduleDTO convertToDto(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMovie(convertToDto(schedule.getMovie()));
        dto.setRoom(convertToDto(schedule.getRoom()));
        dto.setStatus(schedule.getStatus().name());
        return dto;
    }

    private RoomDTO convertToDto(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomId(room.getRoomId());
        dto.setName(room.getName());
        dto.setColumn(room.getColumn());
        dto.setRow(room.getRow());
        dto.setTypeOfRoom(room.getTypeOfRoom().name());
        dto.setStatus(room.getStatus().name());
        return dto;
    }

    private SeatDTO convertToDto(Seat seat) {
        if (seat == null) return null;
        SeatDTO dto = new SeatDTO();
        dto.setSeatId(seat.getSeatId());
        dto.setRoom(convertToDto(seat.getRoom()));
        dto.setSeatType(seat.getSeatType().name());
        dto.setPosition(seat.getPosition());
        dto.setStatus(seat.getStatus().name());
        dto.setIsVIP(seat.getIsVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        return dto;
    }
}