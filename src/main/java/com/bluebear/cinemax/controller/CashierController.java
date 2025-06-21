package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.helper.Helper;
import com.bluebear.cinemax.repository.cashier.SeatRepository;
import com.bluebear.cinemax.service.cashier.CashierService;
import com.bluebear.cinemax.service.cashier.PdfGenerationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.bluebear.cinemax.helper.Helper.*;

@Controller
@RequestMapping("/cashier")
public class CashierController {
    //Xét Date cho ngày hôm này và 7 ngày tới
    private final LocalDateTime currentDate = LocalDateTime.now();
    private final LocalDateTime sevenDaysFromToday = currentDate.plusDays(7);

    private final Integer CASHIER_THEATER_ID = 1;

    private final CashierService cashierService;
    private SeatRepository seatRepository;
    private PdfGenerationService pdfGenerationService;

    @Autowired
    public CashierController(CashierService cashierService, SeatRepository seatRepository, PdfGenerationService pdfGenerationService) {
        this.pdfGenerationService = pdfGenerationService;
        this.seatRepository = seatRepository;
        this.cashierService = cashierService;
    }

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
        clearSessionData(session);
        session.setAttribute(ATTR_THEATER_ID, CASHIER_THEATER_ID);

        try {
            String normalizedKeyword = normalizeSearchParam(keyword);

            Pageable pageable = PageRequest.of(page, 3);

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
            model.addAttribute("availableGenres", availableGenres);
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

        return "cashier-templates/cashier-booking";
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
                return redirectToMovieSelection();
            }
            Page<MovieDTO> moviesPage = cashierService.getPagedMovieByTheater(
                    CASHIER_THEATER_ID, currentDate, Pageable.unpaged());

            Optional<MovieDTO> movieOpt = moviesPage.getContent().stream()
                    .filter(movie -> movie.getMovieId().equals(id))
                    .findFirst();

            if (movieOpt.isEmpty()) {
                return redirectToMovieSelection();
            }

            MovieDTO movie = movieOpt.get();

            session.removeAttribute(ATTR_SELECTED_SCHEDULE);
            session.removeAttribute(ATTR_SELECTED_SEATS);
            session.removeAttribute(ATTR_CUSTOMER_INFO);
            session.removeAttribute(ATTR_PRICE_BREAKDOWN);

            session.setAttribute(ATTR_SELECTED_MOVIE, movie);
            session.setAttribute(ATTR_CURRENT_STEP, 2);


            List<ScheduleDTO> schedules = cashierService.getSchedulesByMovieAndDate(
                    CASHIER_THEATER_ID, id, currentDate, sevenDaysFromToday);

            model.addAttribute(ATTR_SELECTED_MOVIE, movie);
            model.addAttribute(ATTR_CURRENT_STEP, 2);
            model.addAttribute("schedules", schedules);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);
            model.addAttribute("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            return "cashier-templates/cashier-booking";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
    }

    //================SEAT==========================================================
    @PostMapping("/select-seats")
    public String selectSeats(@RequestParam String time,
                              @RequestParam String roomName,
                              @RequestParam String roomType,
                              @RequestParam Integer scheduleId,
                              Model model,
                              HttpSession session) {
        try {
            if (!isValidSession(session)) {
                return "redirect:/cashier/movie/";
            }

            clearSeatSelections(session);

            Map<String, Object> scheduleInfo = createScheduleInfo(time, roomName, roomType, scheduleId);
            session.setAttribute(ATTR_SELECTED_SCHEDULE, scheduleInfo);
            session.setAttribute(ATTR_CURRENT_STEP, 3);

            List<SeatDTO> seats = cashierService.getSeatsWithBookingDetails(scheduleId);

            Map<String, Object> seatGrid = createSimpleSeatGrid(seats, roomType);

            Page<TheaterStockDTO> foodMenu = cashierService.getAvailableTheaterStockByTheater(CASHIER_THEATER_ID, PageRequest.of(0, 4));

            model.addAttribute("theaterStock", foodMenu);
            model.addAttribute(ATTR_SELECTED_MOVIE, session.getAttribute(ATTR_SELECTED_MOVIE));
            model.addAttribute(ATTR_SELECTED_SCHEDULE, scheduleInfo);
            model.addAttribute(ATTR_CURRENT_STEP, 3);
            model.addAttribute("seatGridData", seatGrid);
            model.addAttribute("seats", seats);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);

            return "cashier-templates/cashier-booking";

        } catch (Exception e) {
            System.err.println("Error in selectSeats: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
    }

    private List<Integer> parseSelectedSeats(String selectedSeatsRaw) {
        if (selectedSeatsRaw == null || selectedSeatsRaw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {

            List<Integer> seatIds = Arrays.stream(selectedSeatsRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seat ID: " + s);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // Remove null values
                    .collect(Collectors.toList());

            System.out.println("DEBUG - Parsed seat IDs: " + seatIds);
            return seatIds;

        } catch (Exception e) {
            System.err.println("Error parsing selected seats: " + selectedSeatsRaw + " - " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @PostMapping("/customer-info")
    public String customerInfo(@RequestParam("selectedSeats") String selectedSeatsRaw,
                               @RequestParam(value = "foodPage", defaultValue = "0") int foodPage,
                               @RequestParam(value = "foodSize", defaultValue = "4") int foodSize,
                               Model model, HttpSession session) {
        try {
            // Validate session
            if (!isValidSession(session)) {
                System.err.println("Invalid session");
                return "redirect:/cashier/movie/";
            }

            // Parse selected seats
            List<Integer> seatIds = parseSelectedSeats(selectedSeatsRaw);
            System.out.println("DEBUG - Parsed seat IDs count: " + seatIds.size());

            if (seatIds.isEmpty()) {
                System.err.println("No valid seat IDs found");
                session.setAttribute("seatError", "Vui lòng chọn ít nhất một ghế.");
                return "redirect:/cashier/back-to-seats";
            }


            List<SeatDTO> selectedSeats = Helper.getSeatsByIds(seatIds, seatRepository, cashierService);
            System.out.println("DEBUG - Found seats count: " + selectedSeats.size());

            if (selectedSeats.size() != seatIds.size()) {
                System.err.println("Seat count mismatch. Expected: " + seatIds.size() + ", Found: " + selectedSeats.size());
                session.setAttribute("seatError", "Một số ghế không còn khả dụng.");
                return "redirect:/cashier/back-to-seats";
            }

            // Check if any selected seats are booked
            Map<String, Object> scheduleInfo = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);
            Integer scheduleId = (Integer) scheduleInfo.get("scheduleId");

            List<SeatDTO> currentSeats = cashierService.getSeatsWithBookingDetails(scheduleId);
            Map<Integer, Boolean> seatBookingStatus = currentSeats.stream()
                    .collect(Collectors.toMap(SeatDTO::getSeatId, SeatDTO::getIsBooked));

            List<String> seatPositions = selectedSeats.stream()
                    .map(SeatDTO::getPosition)
                    .collect(Collectors.toList());

            session.setAttribute(ATTR_SELECTED_SEATS, seatPositions);
            session.setAttribute("selectedSeatIds", seatIds);
            session.setAttribute(ATTR_CURRENT_STEP, 4);

            Page<TheaterStockDTO> foodMenu = cashierService.getAvailableTheaterStockByTheater(CASHIER_THEATER_ID, PageRequest.of(foodPage, foodSize));

            model.addAttribute("theaterStock", foodMenu);
            model.addAttribute("currentFoodPage", foodPage);
            model.addAttribute("totalFoodPages", foodMenu.getTotalPages());
            model.addAttribute("hasPreviousFoodPage", foodMenu.hasPrevious());
            model.addAttribute("hasNextFoodPage", foodMenu.hasNext());
            model.addAttribute(ATTR_SELECTED_MOVIE, session.getAttribute(ATTR_SELECTED_MOVIE));
            model.addAttribute(ATTR_SELECTED_SCHEDULE, session.getAttribute(ATTR_SELECTED_SCHEDULE));
            model.addAttribute(ATTR_SELECTED_SEATS, seatPositions);
            model.addAttribute(ATTR_CURRENT_STEP, 4);
            model.addAttribute("theaterId", CASHIER_THEATER_ID);

            return "cashier-templates/cashier-booking";

        } catch (Exception e) {
            System.err.println("Error in customerInfo: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("seatError", "Có lỗi hệ thống, vui lòng thử lại.");
            return "redirect:/cashier/back-to-seats";
        }
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(@RequestParam() Map<String, String> allRequestParams,
                                 Model model, HttpSession session) {
        try {
            Map<Integer, Integer> foodQuantities = new HashMap<>();

            for (Map.Entry<String, String> entry : allRequestParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.startsWith("foodQuantities[")) {
                    String idStr = key.substring(15, key.length() - 1);
                    int id = Integer.parseInt(idStr);
                    int quantity = Integer.parseInt(value);
                    foodQuantities.put(id, quantity);
                }
            }

            Map<Integer, Integer> selectedFood = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : foodQuantities.entrySet()) {
                if (entry.getValue() > 0) {
                    selectedFood.put(entry.getKey(), entry.getValue());
                }
            }

            MovieDTO movie = (MovieDTO) session.getAttribute(ATTR_SELECTED_MOVIE);

            Map<String, Object> scheduleInfo = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);

            List<String> seatPositions = (List<String>) session.getAttribute(ATTR_SELECTED_SEATS);
            List<Integer> selectedSeatIds = (List<Integer>) session.getAttribute("selectedSeatIds");
            String customerName = allRequestParams.get("customerName");
            String customerPhone = allRequestParams.get("customerPhone");
            String customerEmail = allRequestParams.get("customerEmail");

            Integer scheduleId = (Integer) scheduleInfo.get("scheduleId");

            String paymentMethod = allRequestParams.get("paymentMethod");

            BookingRequestDTO bookingRequest = BookingRequestDTO.builder()
                    .customerName(customerName)
                    .customerPhone(customerPhone)
                    .customerEmail(customerEmail)
                    .scheduleId(scheduleId)
                    .selectedSeatIds(selectedSeatIds)
                    .foodQuantities(foodQuantities)
                    //.promotionCode()
                    // .employeeId(employeeId)
                    .build();

            BookingResponseDTO bookingResponse = cashierService.createBooking(bookingRequest);

            if (paymentMethod.equalsIgnoreCase("bank")) {
                model.addAttribute("bookingResponse", bookingResponse);
                return "cashier-templates/cashier-payment-bank";
            }

            model.addAttribute("bookingResult", bookingResponse);
            session.setAttribute("bookingResult", bookingResponse);
            model.addAttribute(ATTR_CURRENT_STEP, 5);

            clearSessionData(session);

            return "cashier-templates/cashier-booking";

        } catch (Exception e) {
            System.err.println("Error in confirmBooking: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
    }

    @GetMapping("/back-to-seats")
    public String backToSeats(HttpSession session, Model model) {
        if (!isValidSession(session)) {
            return "redirect:/cashier/movie/";
        }

        Map<String, Object> scheduleInfo = (Map<String, Object>) session.getAttribute(ATTR_SELECTED_SCHEDULE);
        if (scheduleInfo == null) {
            return "redirect:/cashier/movie/";
        }

        String seatError = (String) session.getAttribute("seatError");
        session.removeAttribute("seatError");

        Integer scheduleId = (Integer) scheduleInfo.get("scheduleId");
        String roomType = (String) scheduleInfo.get("roomType");

        List<SeatDTO> seats = cashierService.getSeatsWithBookingDetails(scheduleId);
        Map<String, Object> seatGrid = createSimpleSeatGrid(seats, roomType);

        model.addAttribute(ATTR_SELECTED_MOVIE, session.getAttribute(ATTR_SELECTED_MOVIE));
        model.addAttribute(ATTR_SELECTED_SCHEDULE, scheduleInfo);
        model.addAttribute(ATTR_CURRENT_STEP, 3);
        model.addAttribute("seatGridData", seatGrid);
        model.addAttribute("seats", seats);
        model.addAttribute("seatError", seatError);
        model.addAttribute("theaterId", CASHIER_THEATER_ID);

        return "cashier-templates/cashier-booking";
    }

    @GetMapping("/invoice/download")
    public String downloadInvoice(HttpServletResponse response, HttpSession session) {
        try {

            BookingResponseDTO bookingResult = (BookingResponseDTO) session.getAttribute("bookingResult");

            if (bookingResult == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy hóa đơn.");
                return "redirect:/cashier/movie/";
            }

            byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(bookingResult);

            response.setContentType("application/pdf");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=invoice_"+ bookingResult.getInvoiceId() + ".pdf";
            response.setHeader(headerKey, headerValue);
            response.setContentLength(pdfBytes.length);

            OutputStream outputStream = response.getOutputStream();
            outputStream.write(pdfBytes);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            System.err.println("Lỗi khi tạo và tải file PDF: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
        return redirectToMovieSelection();
    }

}

