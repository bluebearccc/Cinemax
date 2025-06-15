package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.service.cashier.CashierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping(CashierController.CASHIER_PATH)
public class CashierController {
    private final LocalDate currentDate = LocalDate.now();
    private final CashierService cashierService;

    // CONFIGURE THIS: Set the specific theater ID for this cashier
    private static final Integer CASHIER_THEATER_ID = 1; // Change this to your desired theater ID

    // Path and template constants
    public static final String CASHIER_PATH = "/cashier";
    private static final String CASHIER_BOOKING_VIEW = "cashier-templates/cashier-booking";
    private static final String REDIRECT_CASHIER = "redirect:/cashier/movie/";

    // Session attribute keys
    private static final String ATTR_SELECTED_MOVIE = "selectedMovie";
    private static final String ATTR_SELECTED_SCHEDULE = "selectedSchedule";
    private static final String ATTR_SELECTED_SEATS = "selectedSeats";
    private static final String ATTR_CUSTOMER_INFO = "customerInfo";
    private static final String ATTR_PRICE_BREAKDOWN = "priceBreakdown";
    private static final String ATTR_CURRENT_STEP = "currentStep";
    private static final String ATTR_THEATER_ID = "theaterId";

    // Room type
    private static final String ROOM_TYPE_COUPLE = "Couple";

    // Prices
    private static final int COUPLE_SEAT_PRICE = 190000;
    private static final int VIP_SEAT_PRICE = 95000;
    private static final int NORMAL_SEAT_PRICE = 75000;

    @Autowired
    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("/")
    public String redirectToMovieSelection() {
        return "redirect:/cashier/movie/";
    }

    @GetMapping("/movie/")
    public String selectMovie(Model model,
                              HttpSession session,
                              @RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestParam(required = false) String sort,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer genreId
    ) {
        // Clear all previous session data
        clearSessionData(session);
        session.setAttribute(ATTR_THEATER_ID, CASHIER_THEATER_ID);

        try {
            String normalizedKeyword = normalizeSearchParam(keyword);

            Pageable pageable = PageRequest.of(page, size);

            Page<MovieDTO> moviesPage;

            if (normalizedKeyword != null && genreId != null) {
                moviesPage = cashierService.searchPagedMoviesByTheaterAndGenreAndKeyword(
                        CASHIER_THEATER_ID, genreId, normalizedKeyword, currentDate, pageable);
            } else if (normalizedKeyword != null) {
                moviesPage = cashierService.searchPagedMoviesByTheaterAndKeyword(
                        CASHIER_THEATER_ID, normalizedKeyword, currentDate, pageable);
            } else if (genreId != null) {
                moviesPage = cashierService.getPagedMoviesByTheaterAndGenre(
                        CASHIER_THEATER_ID, genreId, currentDate, pageable);
            } else {
                moviesPage = cashierService.getPagedMovieByTheater(
                        CASHIER_THEATER_ID, currentDate, pageable);
            }

            List<String> availableGenres = getAvailableGenresForTheater(CASHIER_THEATER_ID);

            model.addAttribute("movies", moviesPage.getContent());
            model.addAttribute("moviesPage", moviesPage);
            model.addAttribute("availableGenres", availableGenres != null ? availableGenres : new ArrayList<>());
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("keyword", normalizedKeyword);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);
            model.addAttribute(ATTR_CURRENT_STEP, 1);

            boolean hasSearchCriteria = (normalizedKeyword != null && !normalizedKeyword.isEmpty()) || genreId != null;
            model.addAttribute("hasSearchCriteria", hasSearchCriteria);
            model.addAttribute("totalMovies", moviesPage.getTotalElements());

        } catch (Exception e) {
            e.printStackTrace();
            // If error occurs, show empty list
            model.addAttribute("movies", new ArrayList<>());
            model.addAttribute("moviesPage", Page.empty());
            model.addAttribute("availableGenres", new ArrayList<>());
            model.addAttribute("selectedGenreId", null);
            model.addAttribute("keyword", "");
            model.addAttribute("theaterId", CASHIER_THEATER_ID);
            model.addAttribute(ATTR_CURRENT_STEP, 1);
            model.addAttribute("hasSearchCriteria", false);
            model.addAttribute("totalMovies", 0);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách phim. Vui lòng thử lại.");
        }

        return CASHIER_BOOKING_VIEW;
    }

    @GetMapping("/search/movie")
    public String searchMovie(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer genreId,
                              @RequestParam(defaultValue = "0") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              Model model,
                              HttpSession session) {
        // Redirect to main movie selection with parameters
        return selectMovie(model, session, page, size, null, keyword, genreId);
    }

    @GetMapping("/movie/clear-filters")
    public String clearFilters() {
        return "redirect:/cashier/movie/";
    }

    @GetMapping("/{id}/select-schedule")
    public String selectMovieWithAvailableSchedule(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            Integer sessionTheaterId = (Integer) session.getAttribute(ATTR_THEATER_ID);
            if (sessionTheaterId == null || !sessionTheaterId.equals(CASHIER_THEATER_ID)) {
                return REDIRECT_CASHIER;
            }

            // Get movie from the paged results to verify it exists
            Page<MovieDTO> moviesPage = cashierService.getPagedMovieByTheater(
                    CASHIER_THEATER_ID, currentDate, Pageable.unpaged());

            Optional<MovieDTO> movieOpt = moviesPage.getContent().stream()
                    .filter(movie -> movie.getMovieId().equals(id))
                    .findFirst();

            if (movieOpt.isEmpty()) {
                return REDIRECT_CASHIER;
            }

            MovieDTO movie = movieOpt.get();

            session.removeAttribute(ATTR_SELECTED_SCHEDULE);
            session.removeAttribute(ATTR_SELECTED_SEATS);
            session.removeAttribute(ATTR_CUSTOMER_INFO);
            session.removeAttribute(ATTR_PRICE_BREAKDOWN);

            session.setAttribute(ATTR_SELECTED_MOVIE, movie);
            session.setAttribute(ATTR_CURRENT_STEP, 2);

            List<ScheduleDTO> schedules = cashierService.getSchedulesByMovieAndDate(
                    CASHIER_THEATER_ID, id, currentDate);

            model.addAttribute(ATTR_SELECTED_MOVIE, movie);
            model.addAttribute(ATTR_CURRENT_STEP, 2);
            model.addAttribute("schedules", schedules);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);
            model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            return CASHIER_BOOKING_VIEW;
        } catch (Exception e) {
            e.printStackTrace();
            return REDIRECT_CASHIER;
        }
    }

    @PostMapping("/select-seats")
    public String selectSchedule(@RequestParam String time,
                                 @RequestParam String roomName,
                                 @RequestParam String roomType,
                                 @RequestParam Integer scheduleId,
                                 Model model, HttpSession session) {
        try {
            Integer sessionTheaterId = (Integer) session.getAttribute(ATTR_THEATER_ID);
            if (sessionTheaterId == null || !sessionTheaterId.equals(CASHIER_THEATER_ID)) {
                return REDIRECT_CASHIER;
            }

            MovieDTO selectedMovie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);
            if (selectedMovie == null) {
                return REDIRECT_CASHIER;
            }

            session.removeAttribute(ATTR_SELECTED_SEATS);
            session.removeAttribute(ATTR_CUSTOMER_INFO);
            session.removeAttribute(ATTR_PRICE_BREAKDOWN);

            Map<String, Object> scheduleInfo = new HashMap<>();
            scheduleInfo.put("time", time);
            scheduleInfo.put("roomName", roomName);
            scheduleInfo.put("roomType", roomType);
            scheduleInfo.put("scheduleId", scheduleId);

            session.setAttribute(ATTR_SELECTED_SCHEDULE, scheduleInfo);
            session.setAttribute(ATTR_CURRENT_STEP, 3);

            List<SeatDTO> rawSeats = cashierService.getSeatsWithBookingDetails(scheduleId);
            System.out.println("Raw seats loaded: " + rawSeats.size());

            List<SeatDTO> seats = removeDuplicateSeats(rawSeats);
            System.out.println("Seats after cleanup: " + seats.size());

            // Create seat grid structure with error handling
            Map<String, Object> seatGridData;
            try {
                System.out.println("Creating seat grid for room type: " + roomType);
                seatGridData = createSeatGridStructure(seats, roomType);

                if (seatGridData != null) {
                    Object rows = seatGridData.get("rows");
                    System.out.println("Grid data created:");
                    System.out.println("- Rows: " + (rows != null ? ((List<?>)rows).size() : "null"));
                    System.out.println("- Max columns: " + seatGridData.get("maxColumns"));
                    System.out.println("- Room type: " + seatGridData.get("roomType"));
                } else {
                    System.out.println("Grid data is null!");
                }

            } catch (Exception e) {
                System.err.println("Error creating seat grid structure: " + e.getMessage());
                e.printStackTrace();

                // Fallback: create empty grid
                seatGridData = new HashMap<>();
                seatGridData.put("rows", new ArrayList<>());
                seatGridData.put("maxColumns", 0);
                seatGridData.put("roomType", roomType);
                seatGridData.put("totalRows", 0);

                model.addAttribute("seatError", "Có lỗi khi tải danh sách ghế. Vui lòng thử lại.");
            }

            model.addAttribute(ATTR_SELECTED_MOVIE, selectedMovie);
            model.addAttribute(ATTR_SELECTED_SCHEDULE, scheduleInfo);
            model.addAttribute(ATTR_CURRENT_STEP, 3);
            model.addAttribute("seats", seats);
            model.addAttribute("seatGridData", seatGridData);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);

            return CASHIER_BOOKING_VIEW;

        } catch (Exception e) {
            System.err.println("Error in selectSchedule: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin ghế. Vui lòng thử lại.");
            MovieDTO movie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);
            if (movie != null) {
                return "redirect:/cashier/" + movie.getMovieId() + "/select-schedule";
            }
            return REDIRECT_CASHIER;
        }
    }

    @GetMapping("/back-to-seats")
    public String backToSeats(Model model, HttpSession session) {
        // Verify theater ID in session
        Integer sessionTheaterId = (Integer) session.getAttribute(ATTR_THEATER_ID);
        if (sessionTheaterId == null || !sessionTheaterId.equals(CASHIER_THEATER_ID)) {
            return REDIRECT_CASHIER;
        }

        MovieDTO selectedMovie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);
        @SuppressWarnings("unchecked")
        Map<String, Object> selectedSchedule = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);

        if (selectedMovie == null || selectedSchedule == null) {
            return REDIRECT_CASHIER;
        }

        session.removeAttribute(ATTR_SELECTED_SEATS);
        session.removeAttribute(ATTR_CUSTOMER_INFO);
        session.removeAttribute(ATTR_PRICE_BREAKDOWN);
        session.setAttribute(ATTR_CURRENT_STEP, 3);

        Integer scheduleId = (Integer) selectedSchedule.get("scheduleId");
        String roomType = (String) selectedSchedule.get("roomType");

        List<SeatDTO> rawSeats = cashierService.getSeatsWithBookingDetails(scheduleId);
        List<SeatDTO> seats = removeDuplicateSeats(rawSeats);

        // Create seat grid structure
        Map<String, Object> seatGridData = createSeatGridStructure(seats, roomType);

        model.addAttribute(ATTR_SELECTED_MOVIE, selectedMovie);
        model.addAttribute(ATTR_SELECTED_SCHEDULE, selectedSchedule);
        model.addAttribute(ATTR_CURRENT_STEP, 3);
        model.addAttribute("seats", seats);
        model.addAttribute("seatGridData", seatGridData);
        model.addAttribute("theaterId", CASHIER_THEATER_ID);

        return CASHIER_BOOKING_VIEW;
    }

    @PostMapping("/customer-info")
    public String customerInfo(@RequestParam String selectedSeats,
                               Model model, HttpSession session) {
        try {
            System.out.println("=== DEBUG: Customer Info ===");
            System.out.println("Selected Seats Raw: " + selectedSeats);

            // Verify theater ID in session
            Integer sessionTheaterId = (Integer) session.getAttribute(ATTR_THEATER_ID);
            if (sessionTheaterId == null || !sessionTheaterId.equals(CASHIER_THEATER_ID)) {
                return REDIRECT_CASHIER;
            }

            MovieDTO selectedMovie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);
            @SuppressWarnings("unchecked")
            Map<String, Object> selectedSchedule = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);

            if (selectedMovie == null || selectedSchedule == null) {
                return REDIRECT_CASHIER;
            }

            // Parse selected seats from comma-separated string
            List<String> seatList = new ArrayList<>();
            if (selectedSeats != null && !selectedSeats.trim().isEmpty()) {
                String[] seatArray = selectedSeats.split(",");
                for (String seat : seatArray) {
                    String trimmedSeat = seat.trim();
                    if (!trimmedSeat.isEmpty()) {
                        seatList.add(trimmedSeat);
                    }
                }
            }

            System.out.println("Parsed Seats: " + seatList);

            if (seatList.isEmpty()) {
                session.setAttribute("seatError", "Vui lòng chọn ít nhất một ghế!");
                return "redirect:/cashier/back-to-seats";
            }

            // Save selected seats to session
            session.setAttribute(ATTR_SELECTED_SEATS, seatList);
            session.setAttribute(ATTR_CURRENT_STEP, 4);

            // Get food menu
            List<TheaterStockDTO> foodMenu = cashierService.getAvailableTheaterStockByTheater(CASHIER_THEATER_ID);
            System.out.println("Food menu items: " + (foodMenu != null ? foodMenu.size() : 0));

            // Calculate price breakdown - FIXED: Create proper PriceBreakdownDTO
            PriceBreakdownDTO priceBreakdown = new PriceBreakdownDTO(selectedMovie, seatList, selectedSchedule, new HashMap<>());
            session.setAttribute(ATTR_PRICE_BREAKDOWN, priceBreakdown);

            model.addAttribute(ATTR_SELECTED_MOVIE, selectedMovie);
            model.addAttribute(ATTR_SELECTED_SCHEDULE, selectedSchedule);
            model.addAttribute(ATTR_SELECTED_SEATS, seatList);
            model.addAttribute(ATTR_CURRENT_STEP, 4);
            model.addAttribute("foodMenu", foodMenu);
            model.addAttribute(ATTR_PRICE_BREAKDOWN, priceBreakdown);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);

            return CASHIER_BOOKING_VIEW;

        } catch (Exception e) {
            System.err.println("Error in customerInfo: " + e.getMessage());
            e.printStackTrace();

            session.setAttribute("seatError", "Có lỗi xảy ra khi xử lý ghế đã chọn. Vui lòng thử lại.");
            return "redirect:/cashier/back-to-seats";
        }
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(@RequestParam(required = false) String customerName,
                                 @RequestParam(required = false) String customerPhone,
                                 @RequestParam(required = false) String customerEmail,
                                 @RequestParam(required = false) Map<String, String> foodItems,
                                 Model model, HttpSession session) {
        try {
            Integer sessionTheaterId = (Integer) session.getAttribute(ATTR_THEATER_ID);
            if (sessionTheaterId == null || !sessionTheaterId.equals(CASHIER_THEATER_ID)) {
                return REDIRECT_CASHIER;
            }

            MovieDTO selectedMovie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);
            @SuppressWarnings("unchecked")
            Map<String, Object> selectedSchedule = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);
            @SuppressWarnings("unchecked")
            List<String> selectedSeats = (List<String>) session.getAttribute(ATTR_SELECTED_SEATS);

            if (selectedMovie == null || selectedSchedule == null || selectedSeats == null) {
                return REDIRECT_CASHIER;
            }

            // Save customer info
            Map<String, String> customerInfo = new HashMap<>();
            customerInfo.put("name", customerName);
            customerInfo.put("phone", customerPhone);
            customerInfo.put("email", customerEmail);
            session.setAttribute(ATTR_CUSTOMER_INFO, customerInfo);

            // Get price breakdown from session and update with food
            PriceBreakdownDTO priceBreakdown = (PriceBreakdownDTO) session.getAttribute(ATTR_PRICE_BREAKDOWN);
            if (priceBreakdown == null) {
                // Recreate if missing
                priceBreakdown = new PriceBreakdownDTO(selectedMovie, selectedSeats, selectedSchedule, new HashMap<>());
            }

            // Update with food items
            if (foodItems != null && !foodItems.isEmpty()) {
                updatePriceBreakdownWithFood(priceBreakdown, foodItems);
            }

            session.setAttribute(ATTR_PRICE_BREAKDOWN, priceBreakdown);
            session.setAttribute(ATTR_CURRENT_STEP, 5);

            model.addAttribute(ATTR_SELECTED_MOVIE, selectedMovie);
            model.addAttribute(ATTR_SELECTED_SCHEDULE, selectedSchedule);
            model.addAttribute(ATTR_SELECTED_SEATS, selectedSeats);
            model.addAttribute(ATTR_CUSTOMER_INFO, customerInfo);
            model.addAttribute(ATTR_PRICE_BREAKDOWN, priceBreakdown);
            model.addAttribute(ATTR_CURRENT_STEP, 5);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);

            return CASHIER_BOOKING_VIEW;

        } catch (Exception e) {
            System.err.println("Error in confirmBooking: " + e.getMessage());
            e.printStackTrace();
            return REDIRECT_CASHIER;
        }
    }

    private void clearSessionData(HttpSession session) {
        session.removeAttribute(ATTR_SELECTED_MOVIE);
        session.removeAttribute(ATTR_SELECTED_SCHEDULE);
        session.removeAttribute(ATTR_SELECTED_SEATS);
        session.removeAttribute(ATTR_CUSTOMER_INFO);
        session.removeAttribute(ATTR_PRICE_BREAKDOWN);
        session.removeAttribute(ATTR_CURRENT_STEP);
    }

    private String normalizeSearchParam(String param) {
        if (param == null) return null;
        param = param.trim();
        return param.isEmpty() ? null : param;
    }

    /**
     * Remove duplicate seats based on priority rules
     */
    private List<SeatDTO> removeDuplicateSeats(List<SeatDTO> seats) {
        if (seats == null || seats.isEmpty()) {
            return seats;
        }

        // Group seats by position
        Map<String, List<SeatDTO>> seatsByPosition = seats.stream()
                .collect(Collectors.groupingBy(SeatDTO::getPosition));

        List<SeatDTO> uniqueSeats = new ArrayList<>();

        for (Map.Entry<String, List<SeatDTO>> entry : seatsByPosition.entrySet()) {
            List<SeatDTO> seatsAtPosition = entry.getValue();

            if (seatsAtPosition.size() == 1) {
                uniqueSeats.add(seatsAtPosition.get(0));
            } else {
                // Có duplicate, chọn ghế theo priority
                SeatDTO selectedSeat = seatsAtPosition.stream()
                        .sorted((s1, s2) -> {
                            // Ưu tiên ghế đã book
                            if (s1.getIsBooked() && !s2.getIsBooked()) return -1;
                            if (!s1.getIsBooked() && s2.getIsBooked()) return 1;

                            // Ưu tiên ghế VIP
                            if (s1.getIsVIP() && !s2.getIsVIP()) return -1;
                            if (!s1.getIsVIP() && s2.getIsVIP()) return 1;

                            // Ưu tiên ghế có giá cao hơn
                            if (s1.getUnitPrice() != null && s2.getUnitPrice() != null) {
                                int priceComparison = s2.getUnitPrice().compareTo(s1.getUnitPrice());
                                if (priceComparison != 0) return priceComparison;
                            }

                            // Ưu tiên ghế có ID nhỏ hơn
                            return s1.getSeatId().compareTo(s2.getSeatId());
                        })
                        .findFirst()
                        .orElse(seatsAtPosition.get(0));

                uniqueSeats.add(selectedSeat);

                System.err.println("Removed duplicate seats at position " + entry.getKey() +
                        ". Selected seat ID: " + selectedSeat.getSeatId());
            }
        }

        return uniqueSeats;
    }

    private void updatePriceBreakdownWithFood(PriceBreakdownDTO priceBreakdown, Map<String, String> foodItems) {
        if (priceBreakdown == null || foodItems == null || foodItems.isEmpty()) {
            return;
        }

        List<TheaterStockDTO> availableFood = cashierService.getAvailableTheaterStockByTheater(CASHIER_THEATER_ID);
        Map<Integer, TheaterStockDTO> foodMap = availableFood.stream()
                .collect(Collectors.toMap(TheaterStockDTO::getTheaterStockId, food -> food));

        // Clear existing food items
        priceBreakdown.getFoodItems().clear();

        for (Map.Entry<String, String> entry : foodItems.entrySet()) {
            if (entry.getKey().startsWith("food_")) {
                try {
                    Integer foodId = Integer.parseInt(entry.getKey().replace("food_", ""));
                    Integer quantity = Integer.parseInt(entry.getValue());

                    if (quantity > 0 && foodMap.containsKey(foodId)) {
                        TheaterStockDTO food = foodMap.get(foodId);

                        // Create food item using the inner class
                        PriceBreakdownDTO.FoodItem foodItem = new PriceBreakdownDTO.FoodItem(
                                food.getFoodName(),
                                quantity,
                                food.getUnitPrice()
                        );

                        priceBreakdown.getFoodItems().add(foodItem);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid food item format: " + entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        // Recalculate total
        priceBreakdown.recalculateTotal();
    }

    private List<String> getAvailableGenresForTheater(Integer theaterId) {
        // This is a placeholder - you would need to implement this in your service
        return Arrays.asList("Hành động", "Hài", "Kinh dị", "Lãng mạn", "Khoa học viễn tưởng");
    }

    private Map<String, Object> createSeatGridStructure(List<SeatDTO> seats, String roomType) {
        System.out.println("=== DEBUG: Creating seat grid for room type: " + roomType + " ===");

        // Handle couple room type differently
        if (ROOM_TYPE_COUPLE.equalsIgnoreCase(roomType)) {
            System.out.println("Redirecting to couple seat grid creation");
            return createCoupleSeatGridStructure(seats);
        }

        Map<String, Object> gridData = new HashMap<>();

        if (seats == null || seats.isEmpty()) {
            gridData.put("rows", new ArrayList<>());
            gridData.put("maxColumns", 0);
            gridData.put("roomType", roomType);
            gridData.put("totalRows", 0);
            return gridData;
        }

        // Create seat map by position
        Map<String, SeatDTO> seatMap = seats.stream()
                .collect(Collectors.toMap(SeatDTO::getPosition, seat -> seat));

        // Analyze seat structure
        Set<Character> rowChars = new TreeSet<>();
        Set<Integer> columnNumbers = new TreeSet<>();

        for (SeatDTO seat : seats) {
            String position = seat.getPosition();
            if (position != null && position.length() >= 2) {
                char rowChar = position.charAt(0);
                try {
                    // Handle both A1 and A01 formats
                    String colStr = position.substring(1);
                    int colNum = Integer.parseInt(colStr);
                    rowChars.add(rowChar);
                    columnNumbers.add(colNum);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid seat position format: " + position);
                }
            }
        }

        // Create seat rows
        List<Map<String, Object>> rows = new ArrayList<>();
        int maxColumns = columnNumbers.isEmpty() ? 0 : Collections.max(columnNumbers);

        for (Character rowChar : rowChars) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("rowLabel", String.valueOf(rowChar));

            List<Map<String, Object>> rowSeats = new ArrayList<>();

            for (int col = 1; col <= maxColumns; col++) {
                // Try both A1 and A01 formats
                String seatPosition1 = rowChar + String.valueOf(col);
                String seatPosition2 = rowChar + String.format("%02d", col);

                SeatDTO seat = seatMap.get(seatPosition1);
                if (seat == null) {
                    seat = seatMap.get(seatPosition2);
                }

                Map<String, Object> seatData = new HashMap<>();

                if (seat != null) {
                    seatData.put("position", seat.getPosition());
                    seatData.put("seatId", seat.getSeatId());
                    seatData.put("isBooked", seat.getIsBooked());
                    seatData.put("isVIP", seat.getIsVIP());
                    seatData.put("seatType", seat.getSeatType());
                    seatData.put("unitPrice", seat.getUnitPrice());
                    seatData.put("exists", true);

                    // Determine seat status
                    if (seat.getIsBooked()) {
                        seatData.put("status", "occupied");
                    } else {
                        seatData.put("status", "available");
                    }

                    // Determine CSS class
                    String cssClass = "seat ";
                    if (seat.getIsBooked()) {
                        cssClass += "occupied";
                    } else {
                        cssClass += "available";
                    }

                    if (seat.getIsVIP()) {
                        cssClass += " vip";
                    }

                    seatData.put("cssClass", cssClass);

                } else {
                    // Placeholder for non-existing seat
                    seatData.put("position", "");
                    seatData.put("exists", false);
                    seatData.put("cssClass", "seat-placeholder");
                }

                rowSeats.add(seatData);
            }

            rowData.put("seats", rowSeats);
            rows.add(rowData);
        }

        gridData.put("rows", rows);
        gridData.put("maxColumns", maxColumns);
        gridData.put("roomType", roomType);
        gridData.put("totalRows", rowChars.size());

        System.out.println("Regular seat grid created: " + rowChars.size() + " rows, " + maxColumns + " columns");
        return gridData;
    }

    /**
     * Create couple seat grid structure for display - FIXED VERSION
     */
    private Map<String, Object> createCoupleSeatGridStructure(List<SeatDTO> seats) {
        System.out.println("=== DEBUG: Creating COUPLE seat grid ===");
        System.out.println("Input seats count: " + (seats != null ? seats.size() : "null"));

        Map<String, Object> gridData = new HashMap<>();

        if (seats == null || seats.isEmpty()) {
            System.out.println("No seats provided for couple room");
            gridData.put("rows", new ArrayList<>());
            gridData.put("maxColumns", 0);
            gridData.put("roomType", ROOM_TYPE_COUPLE);
            gridData.put("totalRows", 0);
            return gridData;
        }

        // Debug: Print all seats
        for (SeatDTO seat : seats) {
            System.out.println("Couple Seat: " + seat.getPosition() +
                    " | Booked: " + seat.getIsBooked() +
                    " | SeatType: " + seat.getSeatType() +
                    " | ID: " + seat.getSeatId());
        }

        // Create seat map by position
        Map<String, SeatDTO> seatMap = seats.stream()
                .collect(Collectors.toMap(SeatDTO::getPosition, seat -> seat));

        // Analyze couple seat structure
        Set<Character> rowChars = new TreeSet<>();
        Set<Integer> columnNumbers = new TreeSet<>();

        for (SeatDTO seat : seats) {
            String position = seat.getPosition();
            if (position != null && position.length() >= 2) {
                char rowChar = position.charAt(0);
                try {
                    // Handle both A1 and A01 formats
                    String colStr = position.substring(1);
                    int colNum = Integer.parseInt(colStr);
                    rowChars.add(rowChar);
                    columnNumbers.add(colNum);
                    System.out.println("Parsed couple seat: Row=" + rowChar + ", Col=" + colNum);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid couple seat position format: " + position);
                }
            }
        }

        System.out.println("Couple room rows: " + rowChars);
        System.out.println("Couple room columns: " + columnNumbers);

        // Create couple seat rows
        List<Map<String, Object>> rows = new ArrayList<>();
        int maxColumns = columnNumbers.isEmpty() ? 0 : Collections.max(columnNumbers);

        for (Character rowChar : rowChars) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("rowLabel", String.valueOf(rowChar));

            List<Map<String, Object>> rowSeats = new ArrayList<>();

            for (int col = 1; col <= maxColumns; col++) {
                // Try both A1 and A01 formats
                String seatPosition1 = rowChar + String.valueOf(col);
                String seatPosition2 = rowChar + String.format("%02d", col);

                SeatDTO seat = seatMap.get(seatPosition1);
                if (seat == null) {
                    seat = seatMap.get(seatPosition2);
                }

                Map<String, Object> seatData = new HashMap<>();

                if (seat != null) {
                    System.out.println("Creating couple seat data for: " + seat.getPosition());

                    seatData.put("position", seat.getPosition());
                    seatData.put("seatId", seat.getSeatId());
                    seatData.put("isBooked", seat.getIsBooked());
                    seatData.put("isVIP", false); // Couple seats are not VIP
                    seatData.put("seatType", "COUPLE");
                    seatData.put("unitPrice", seat.getUnitPrice());
                    seatData.put("exists", true);

                    // Determine couple seat status and CSS
                    if (seat.getIsBooked()) {
                        seatData.put("status", "occupied");
                        seatData.put("cssClass", "seat couple occupied");
                    } else {
                        seatData.put("status", "available");
                        seatData.put("cssClass", "seat couple available");
                    }

                    System.out.println("Couple seat " + seat.getPosition() + " - CSS: " + seatData.get("cssClass"));

                } else {
                    // Placeholder for non-existing couple seat
                    System.out.println("Creating placeholder for position: " + rowChar + col);
                    seatData.put("position", "");
                    seatData.put("exists", false);
                    seatData.put("cssClass", "seat-placeholder");
                }

                rowSeats.add(seatData);
            }

            rowData.put("seats", rowSeats);
            rows.add(rowData);
            System.out.println("Added couple row " + rowChar + " with " + rowSeats.size() + " seats");
        }

        gridData.put("rows", rows);
        gridData.put("maxColumns", maxColumns);
        gridData.put("roomType", ROOM_TYPE_COUPLE);
        gridData.put("totalRows", rowChars.size());

        System.out.println("Couple seat grid completed: " + rowChars.size() + " rows, " + maxColumns + " columns");
        return gridData;
    }
    
}