package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cashier")
public class CashierController {

    private final LocalDateTime currentDate = LocalDateTime.now();
    private final LocalDateTime sevenDate = currentDate.plusDays(7);

    private final Integer theaterId = 1;

    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private DetailSeatService detailSeatService;

    @Autowired
    private TheaterStockService theaterStockService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private BookingService bookingService;

    @GetMapping({"", "/", "/movie/"})
    public String getMovie(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Integer genreId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime date,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(defaultValue = "6") Integer pageSize,
                           HttpSession session,
                           Model model) {
        clearSessionData(session);
        session.setAttribute("theaterId", theaterId);
        try {

            String normalizedKeyword = normalizeSearchParam(keyword);

            LocalDateTime startDate;
            LocalDateTime endDate;

            if (date != null) {
                startDate = date.toLocalDate().atStartOfDay();
                endDate = date.toLocalDate().atTime(23, 59, 59);
            } else {
                startDate = currentDate;
                endDate = sevenDate;
            }

            Pageable pageable = PageRequest.of(page, pageSize);

            Page<MovieDTO> moviesPage = movieService.findMoviesByTheaterAndDateRange(
                    theaterId, Movie_Status.Active, Theater_Status.Active,
                    startDate, endDate, pageable);

            if (normalizedKeyword != null && genreId != null) {
                moviesPage = movieService.findMoviesByTheaterAndGenreAndKeywordAndDateRange(
                        theaterId, genreId, keyword, Movie_Status.Active, Theater_Status.Active,
                        startDate, endDate, pageable);
            } else if (normalizedKeyword != null) {
                moviesPage = movieService.findMoviesByTheaterAndKeywordAndDateRange(
                        theaterId, keyword, Movie_Status.Active, Theater_Status.Active,
                        startDate, endDate, pageable);
            } else if (genreId != null) {
                moviesPage = movieService.findMoviesByTheaterAndGenreAndDateRange(
                        theaterId, genreId, Movie_Status.Active, Theater_Status.Active,
                        startDate, endDate, pageable);
            }

            List<GenreDTO> availableGenres = genreService.getAllGenres();

            List<LocalDateTime> availableDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                availableDates.add(LocalDateTime.now().plusDays(i).toLocalDate().atStartOfDay());
            }

            model.addAttribute("movies", moviesPage.getContent());
            model.addAttribute("moviesPage", moviesPage);
            model.addAttribute("keyword", normalizedKeyword);
            model.addAttribute("availableGenres", availableGenres);
            model.addAttribute("availableDates", availableDates);
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("selectedDate", date);
            model.addAttribute("currentDate", currentDate);
            model.addAttribute("sevenDate", sevenDate);
            session.setAttribute("theaterID", theaterId);
            model.addAttribute("currentStep", 1);
            model.addAttribute("totalMovies", moviesPage.getTotalElements());

            boolean hasSearchCriteria = (normalizedKeyword != null && !normalizedKeyword.isEmpty())
                    || genreId != null
                    || date != null;
            model.addAttribute("hasSearchCriteria", hasSearchCriteria);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("movies", new ArrayList<>());
            model.addAttribute("moviesPage", Page.empty());
            model.addAttribute("availableGenres", new ArrayList<>());
            model.addAttribute("availableDates", new ArrayList<>());
            model.addAttribute("selectedGenreId", null);
            model.addAttribute("selectedDate", null);
            model.addAttribute("keyword", "");
            model.addAttribute("theaterId", theaterId);
            model.addAttribute("currentStep", 1);
            model.addAttribute("hasSearchCriteria", false);
            model.addAttribute("totalMovies", 0);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải danh sách phim. Vui lòng thử lại.");
        }

        return "cashier/cashier-booking";
    }

    @GetMapping("/{movieID}/select-schedule")
    public String getSchedule(@PathVariable Integer movieID,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer theaterId = session.getAttribute("theaterID") != null ? (Integer) session.getAttribute("theaterID") : null;

            if (theaterId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Theater not found");
                return "redirect:/cashier/movie/";
            }

            MovieDTO selectedMovie = movieService.findById(movieID);

            if (selectedMovie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Movie not found");
                return "redirect:/cashier/movie/";
            }

            List<LocalDateTime> availableDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                availableDates.add(LocalDateTime.now().plusDays(i).toLocalDate().atStartOfDay());
            }

            Page<ScheduleDTO> schedules = scheduleService.getSchedulesByMovieIdAndDate(theaterId, movieID, currentDate, sevenDate, Pageable.unpaged());

            for (ScheduleDTO schedule : schedules.getContent()) {
                List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(schedule.getRoomID());
                List<Integer> bookedSeatIds = detailSeatService.findBookedSeatIdsByScheduleId(schedule.getScheduleID());
                SeatAvailabilityDTO seatAvailability = new SeatAvailabilityDTO();

                int totalSeats = allSeatsInRoom.size();
                int availableSeats = totalSeats - bookedSeatIds.size();
                int totalVipSeats = (int) allSeatsInRoom.stream().filter(SeatDTO::getIsVIP).count();
                int totalRegularSeats = totalSeats - totalVipSeats;
                int bookedVipSeats = (int) allSeatsInRoom.stream()
                        .filter(seat -> bookedSeatIds.contains(seat.getSeatID()) && seat.getIsVIP())
                        .count();
                int bookedRegularSeats = bookedSeatIds.size() - bookedVipSeats;

                seatAvailability.setTotalSeats(totalSeats);
                seatAvailability.setAvailableSeats(availableSeats);
                seatAvailability.setTotalVipSeats(totalVipSeats);
                seatAvailability.setAvailableVipSeats(totalVipSeats - bookedVipSeats);
                seatAvailability.setTotalRegularSeats(totalRegularSeats);
                seatAvailability.setAvailableRegularSeats(totalRegularSeats - bookedRegularSeats);
                schedule.setSeatAvailability(seatAvailability);
            }

            session.setAttribute("selectedMovie", selectedMovie);
            model.addAttribute("currentStep", 2);
            model.addAttribute("schedules", schedules);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("theaterId", theaterId);
            model.addAttribute("availableDates", availableDates);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
        return "cashier/cashier-booking";
    }

    @PostMapping("/select-seats")
    public String selectSeats(@RequestParam Integer scheduleId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer theaterId = (Integer) session.getAttribute("theaterID");
            MovieDTO selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");

            if (theaterId == null || selectedMovie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired");
                return "redirect:/cashier/movie/";
            }

            ScheduleDTO selectedSchedule = scheduleService.getScheduleById(scheduleId);
            if (selectedSchedule == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Schedule not found");
                return "redirect:/cashier/" + selectedMovie.getMovieID() + "/select-schedule";
            }

            List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(selectedSchedule.getRoomID());
            List<Integer> bookedSeatIds = detailSeatService.findBookedSeatIdsByScheduleId(scheduleId);

            Map<String, List<SeatDTO>> seatsByRow = allSeatsInRoom.stream()
                    .sorted(Comparator.comparing(s -> Integer.parseInt(s.getPosition().substring(1))))
                    .collect(Collectors.groupingBy(
                            s -> s.getPosition().substring(0, 1),
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            session.setAttribute("selectedSchedule", selectedSchedule);

            model.addAttribute("currentStep", 3);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("seatsByRow", seatsByRow);
            model.addAttribute("bookedSeatIds", bookedSeatIds);
            model.addAttribute("theaterId", theaterId);

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading seats");
            return "redirect:/cashier/movie/";
        }

        return "cashier/cashier-booking";
    }

    @GetMapping("/back-to-seats")
    public String goBackToSeats(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        MovieDTO selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
        ScheduleDTO selectedSchedule = (ScheduleDTO) session.getAttribute("selectedSchedule");

        if (selectedMovie == null || selectedSchedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc đã hết hạn. Vui lòng bắt đầu lại.");
            return "redirect:/cashier/movie/";
        }
        return selectSeats(selectedSchedule.getScheduleID(), session, model, redirectAttributes);
    }

    @PostMapping("/cus-info")
    public String getCustomerInfo(@RequestParam("seatIDs") String seatIDs,
                                  @RequestParam(required = false) String foodKeyword,
                                  @RequestParam(defaultValue = "0") int foodPage,
                                  HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        MovieDTO selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
        ScheduleDTO selectedSchedule = (ScheduleDTO) session.getAttribute("selectedSchedule");

        if (selectedMovie == null || selectedSchedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired or invalid.");
            return "redirect:/cashier/movie/";
        }

        if (seatIDs == null || seatIDs.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one seat.");
            return "redirect:/cashier/" + selectedMovie.getMovieID() + "/select-schedule";
        }

        Integer theaterId = (Integer) session.getAttribute("theaterID");
        if (theaterId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Theater information not found in session.");
            return "redirect:/cashier/movie/";
        }

        session.setAttribute("selectedSeatIds", seatIDs);

        List<String> selectedSeatIdsList = Arrays.asList(seatIDs.split(","));
        List<SeatDTO> selectedSeatsInfo = seatService.getSeatsByIds(selectedSeatIdsList.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
        List<String> seatPositions = selectedSeatsInfo.stream().map(SeatDTO::getPosition).collect(Collectors.toList());

        Pageable foodPageable = PageRequest.of(foodPage, 4);
        Page<TheaterStockDTO> theaterStockPage = theaterStockService.findAvailableByTheaterIdAndKeyword(theaterId, foodKeyword, foodPageable);

        List<PromotionDTO> promotions = promotionService.getActivePromotions();

        model.addAttribute("promotions", promotions);
        model.addAttribute("selectedSeats", seatPositions);
        model.addAttribute("selectedSeatIds", seatIDs);
        model.addAttribute("theaterStockPage", theaterStockPage);
        model.addAttribute("foodKeyword", foodKeyword);
        model.addAttribute("selectedMovie", selectedMovie);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("currentStep", 4);
        // Add an empty bookingRequest object to avoid null pointer on first load
        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }

        return "cashier/cashier-booking";
    }

    @PostMapping("/confirm-booking")
    public String confirmBooking(BookingRequestDTO bookingRequest, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            BookingResultDTO result = bookingService.createBooking(bookingRequest);

            model.addAttribute("bookingResult", result);
            session.setAttribute("bookingResult", result);

            model.addAttribute("currentStep", 5);

            return "cashier/cashier-booking";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Lỗi đặt vé: " + e.getMessage());
            MovieDTO selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
            ScheduleDTO selectedSchedule = (ScheduleDTO) session.getAttribute("selectedSchedule");
            String seatIDs = (String) session.getAttribute("selectedSeatIds");
            if (selectedMovie == null || selectedSchedule == null || seatIDs == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phiên làm việc đã hết hạn. Vui lòng thử lại.");
                return "redirect:/cashier/movie/";
            }
            List<String> selectedSeatIdsList = Arrays.asList(seatIDs.split(","));
            List<SeatDTO> selectedSeatsInfo = seatService.getSeatsByIds(selectedSeatIdsList.stream()
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()));
            List<String> seatPositions = selectedSeatsInfo.stream().map(SeatDTO::getPosition).collect(Collectors.toList());

            Integer theaterId = (Integer) session.getAttribute("theaterID");
            Page<TheaterStockDTO> theaterStockPage = theaterStockService.findAvailableByTheaterIdAndKeyword(theaterId, null, PageRequest.of(0, 4));
            List<PromotionDTO> promotions = promotionService.getActivePromotions();

            model.addAttribute("promotions", promotions);
            model.addAttribute("selectedSeats", seatPositions);
            model.addAttribute("selectedSeatIds", seatIDs);
            model.addAttribute("theaterStockPage", theaterStockPage);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("currentStep", 4);
            model.addAttribute("bookingRequest", bookingRequest);

            return "cashier/cashier-booking";
        }
    }

    @GetMapping("/print-ticket/{invoiceId}")
    public String printTicket(@PathVariable Integer invoiceId, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            BookingResultDTO bookingResult = session.getAttribute("bookingResult") != null ? (BookingResultDTO) session.getAttribute("bookingResult") : null;

            if (bookingResult == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin hóa đơn.");
                return "redirect:/cashier/";
            }

            model.addAttribute("bookingResult", bookingResult);
            model.addAttribute("printDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            return "cashier/print-ticket";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo vé để in: " + e.getMessage());
            return "redirect:/cashier/";
        }
    }

    public static String normalizeSearchParam(String param) {
        if (param == null) return null;
        param = param.trim();
        return param.isEmpty() ? null : param;
    }

    public static void clearSessionData(HttpSession session) {
        session.removeAttribute("selectedMovie");
        session.removeAttribute("selectedSchedule");
        session.removeAttribute("selectedSeatIds");
    }
}