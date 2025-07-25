package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.AgeLimit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.service.detailseat.DetailSeatService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.promotion.PromotionService;
import com.bluebear.cinemax.service.room.RoomService; // IMPORT THÊM
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.seat.SeatService;
import com.bluebear.cinemax.service.theaterstock.TheaterStockService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cashier")
public class BookingProcessController {

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
    private RoomService roomService; // INJECT RoomService

    private final LocalDateTime currentDate = LocalDateTime.now();
    private Integer theaterId = null;

    // ... (Các phương thức khác không thay đổi)
    @GetMapping("/movie/")
    public String getMovie(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Integer genreId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime date,
                           @RequestParam(required = false) AgeLimit ageLimit,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(defaultValue = "6") Integer pageSize,
                           HttpSession session,
                           Model model) {
        EmployeeDTO employee = (EmployeeDTO) session.getAttribute("employee");
        theaterId = employee.getTheaterId();
        session.setAttribute("theaterId", theaterId);
        try {
            String normalizedKeyword = normalizeSearchParam(keyword);

            LocalDateTime queryStartDate;
            LocalDateTime queryEndDate;

            if (date != null) {
                queryStartDate = date.toLocalDate().atStartOfDay();
                queryEndDate = date.toLocalDate().atTime(23, 59, 59);
            } else {
                queryStartDate = currentDate;
                queryEndDate = null;
            }

            List<AgeLimit> applicableAgeLimits = null;
            if (ageLimit != null) {
                applicableAgeLimits = new ArrayList<>();
                switch (ageLimit) {
                    case AGE_18_PLUS:
                        applicableAgeLimits.add(AgeLimit.AGE_18_PLUS);
                    case AGE_16_PLUS:
                        applicableAgeLimits.add(AgeLimit.AGE_16_PLUS);
                    case AGE_13_PLUS:
                        applicableAgeLimits.add(AgeLimit.AGE_13_PLUS);
                    case AGE_P: applicableAgeLimits.add(AgeLimit.AGE_P);
                        break;
                }
            }

            Pageable pageable = PageRequest.of(page, pageSize);
            Page<MovieDTO> moviesPage;

            if (normalizedKeyword != null && genreId != null) {
                moviesPage = movieService.findMoviesForCashierByAllFilters(theaterId, genreId, normalizedKeyword, Movie_Status.Active, Theater_Status.Active, queryStartDate, queryEndDate, applicableAgeLimits, pageable);
            } else if (normalizedKeyword != null) {
                moviesPage = movieService.findMoviesForCashierByKeyword(theaterId, normalizedKeyword, Movie_Status.Active, Theater_Status.Active, queryStartDate, queryEndDate, applicableAgeLimits, pageable);
            } else if (genreId != null) {
                moviesPage = movieService.findMoviesForCashierByGenre(theaterId, genreId, Movie_Status.Active, Theater_Status.Active, queryStartDate, queryEndDate, applicableAgeLimits, pageable);
            } else {
                moviesPage = movieService.findMoviesForCashier(theaterId, Movie_Status.Active, Theater_Status.Active, queryStartDate, queryEndDate, applicableAgeLimits, pageable);
            }

            List<GenreDTO> availableGenres = genreService.getAllGenres();

            List<LocalDate> availableDates = movieService.getAvailableScheduleDatesForCashier(theaterId, currentDate, null);

            model.addAttribute("movies", moviesPage.getContent());
            model.addAttribute("moviesPage", moviesPage);
            model.addAttribute("keyword", normalizedKeyword);
            model.addAttribute("availableGenres", availableGenres);
            model.addAttribute("availableDates", availableDates);
            model.addAttribute("availableAgeLimits", AgeLimit.values());
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedAgeLimit", ageLimit);
            model.addAttribute("currentStep", 1);
            model.addAttribute("totalMovies", moviesPage.getTotalElements());
            model.addAttribute("hasSearchCriteria", (normalizedKeyword != null && !normalizedKeyword.isEmpty()) || genreId != null || date != null || ageLimit != null);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading films, please reload the page.");
        }
        return "cashier/cashier-booking";
    }

    @GetMapping("/{movieID}/select-schedule")
    public String getSchedule(@PathVariable Integer movieID, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            Integer theaterId = (Integer) session.getAttribute("theaterId");
            if (theaterId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Theater not found in session. Please start over.");
                return "redirect:/cashier/movie/";
            }
            MovieDTO selectedMovie = movieService.findById(movieID);
            if (selectedMovie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Movie not found");
                return "redirect:/cashier/movie/";
            }

            LocalDateTime farFutureDate = currentDate.plusYears(10);
            Page<ScheduleDTO> schedules = scheduleService.getSchedulesByMovieIdAndDate(theaterId, movieID, currentDate, farFutureDate, Pageable.unpaged());

            for (ScheduleDTO schedule : schedules.getContent()) {
                List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(schedule.getRoomID()).getContent();
                List<Integer> bookedSeatIdList = detailSeatService.findBookedSeatIdsByScheduleId(schedule.getScheduleID());
                Map<Integer, SeatDTO> allSeatsMap = allSeatsInRoom.stream().collect(Collectors.toMap(SeatDTO::getSeatID, s -> s));

                long getSeatCapacity = allSeatsInRoom.stream()
                        .mapToLong(seat -> (seat.getSeatType() != null && "Couple".equals(seat.getSeatType().name())) ? 2 : 1)
                        .sum();

                int totalCapacity = (int) getSeatCapacity;

                int totalVipCapacity = (int) allSeatsInRoom.stream()
                        .filter(SeatDTO::getIsVIP)
                        .mapToLong(seat -> (seat.getSeatType() != null && "Couple".equals(seat.getSeatType().name())) ? 2 : 1)
                        .sum();

                int totalRegularCapacity = totalCapacity - totalVipCapacity;

                int bookedCapacity = (int) bookedSeatIdList.stream()
                        .map(allSeatsMap::get)
                        .filter(Objects::nonNull)
                        .mapToLong(seat -> (seat.getSeatType() != null && "Couple".equals(seat.getSeatType().name())) ? 2 : 1)
                        .sum();

                int bookedVipCapacity = (int) bookedSeatIdList.stream()
                        .map(allSeatsMap::get)
                        .filter(Objects::nonNull)
                        .filter(SeatDTO::getIsVIP)
                        .mapToLong(seat -> (seat.getSeatType() != null && "Couple".equals(seat.getSeatType().name())) ? 2 : 1)
                        .sum();

                int bookedRegularCapacity = bookedCapacity - bookedVipCapacity;

                SeatAvailabilityDTO availability = new SeatAvailabilityDTO();
                availability.setTotalSeats(totalCapacity);
                availability.setAvailableSeats(totalCapacity - bookedCapacity);
                availability.setTotalVipSeats(totalVipCapacity);
                availability.setAvailableVipSeats(totalVipCapacity - bookedVipCapacity);
                availability.setTotalRegularSeats(totalRegularCapacity);
                availability.setAvailableRegularSeats(totalRegularCapacity - bookedRegularCapacity);
                schedule.setSeatAvailability(availability);
            }

            Map<LocalDate, List<ScheduleDTO>> schedulesByDate = schedules.getContent().stream()
                    .collect(Collectors.groupingBy(
                            schedule -> schedule.getStartTime().toLocalDate(),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            session.setAttribute("selectedMovie", selectedMovie);
            model.addAttribute("currentStep", 2);
            model.addAttribute("schedulesByDate", schedulesByDate);
            model.addAttribute("selectedMovie", selectedMovie);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/movie/";
        }
        return "cashier/cashier-booking";
    }


    @PostMapping("/select-seats")
    public String selectSeats(@RequestParam Integer scheduleId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieDTO selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
            if (selectedMovie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired");
                return "redirect:/cashier/movie/";
            }
            ScheduleDTO selectedSchedule = scheduleService.getScheduleById(scheduleId);
            if (selectedSchedule == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Schedule not found");
                return "redirect:/cashier/" + selectedMovie.getMovieID() + "/select-schedule";
            }

            RoomDTO room = roomService.getRoomById(selectedSchedule.getRoomID());
            List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(selectedSchedule.getRoomID()).getContent();
            List<Integer> bookedSeatIds = detailSeatService.findBookedSeatIdsByScheduleId(scheduleId);

            Map<Character, List<SeatDTO>> seatsByRow = allSeatsInRoom.stream()
                    .collect(Collectors.groupingBy(
                            seat -> seat.getPosition().charAt(0),
                            TreeMap::new,
                            Collectors.toList()
                    ));

            session.setAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("currentStep", 3);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("room", room);

            model.addAttribute("seatsByRow", seatsByRow);

            model.addAttribute("bookedSeatIds", bookedSeatIds);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading seats");
            return "redirect:/cashier/movie/";
        }
        return "cashier/cashier-booking";
    }

    @GetMapping("/back-to-seats")
    public String goBackToSeats(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        ScheduleDTO selectedSchedule = (ScheduleDTO) session.getAttribute("selectedSchedule");
        if (selectedSchedule == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired");
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
        Integer theaterId = (Integer) session.getAttribute("theaterId");

        if (selectedMovie == null || selectedSchedule == null || theaterId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired or invalid.");
            return "redirect:/cashier/movie/";
        }
        if (seatIDs == null || seatIDs.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one seat.");
            return "redirect:/cashier/" + selectedMovie.getMovieID() + "/select-schedule";
        }

        session.setAttribute("selectedSeatIds", seatIDs);
        List<String> selectedSeatIdsList = Arrays.asList(seatIDs.split(","));
        List<SeatDTO> selectedSeatsInfo = seatService.getSeatsByIds(selectedSeatIdsList.stream().map(Integer::parseInt).collect(Collectors.toList()));

        Pageable foodPageable = PageRequest.of(foodPage, 4);
        Page<TheaterStockDTO> theaterStockPage = theaterStockService.findAvailableByTheaterIdAndKeyword(theaterId, foodKeyword, foodPageable);
        List<PromotionDTO> promotions = promotionService.getActivePromotions();

        model.addAttribute("promotions", promotions);
        model.addAttribute("selectedSeats", selectedSeatsInfo.stream().map(SeatDTO::getPosition).collect(Collectors.toList()));
        model.addAttribute("selectedSeatIds", seatIDs);
        model.addAttribute("theaterStockPage", theaterStockPage);
        model.addAttribute("foodKeyword", foodKeyword);
        model.addAttribute("selectedMovie", selectedMovie);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("currentStep", 4);
        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }
        return "cashier/cashier-booking";
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