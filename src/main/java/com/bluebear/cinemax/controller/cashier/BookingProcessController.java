package com.bluebear.cinemax.controller.cashier;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.Age_Limit;
import com.bluebear.cinemax.enumtype.Movie_Status;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.service.detailseat.DetailSeatService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.promotion.PromotionService;
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

    private final LocalDateTime currentDate = LocalDateTime.now();
    private final LocalDateTime sevenDate = currentDate.plusDays(7);
    private Integer theaterId = 1;

    @GetMapping("/movie/")
    public String getMovie(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Integer genreId,
                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime date,
                           @RequestParam(required = false) Age_Limit ageLimit,
                           @RequestParam(defaultValue = "0") Integer page,
                           @RequestParam(defaultValue = "6") Integer pageSize,
                           HttpSession session,
                           Model model) {
        clearSessionData(session);
        session.setAttribute("theaterId", theaterId);
        try {
            String normalizedKeyword = normalizeSearchParam(keyword);
            LocalDateTime startDate = (date != null) ? date.toLocalDate().atStartOfDay() : currentDate;
            LocalDateTime endDate = (date != null) ? date.toLocalDate().atTime(23, 59, 59) : sevenDate;

            List<Age_Limit> applicableAgeLimits = null;
            if (ageLimit != null) {
                applicableAgeLimits = new ArrayList<>();
                switch (ageLimit) {
                    case AGE_18_PLUS:
                        applicableAgeLimits.add(Age_Limit.AGE_18_PLUS);

                    case AGE_16_PLUS:
                        applicableAgeLimits.add(Age_Limit.AGE_16_PLUS);

                    case AGE_13_PLUS:
                        applicableAgeLimits.add(Age_Limit.AGE_13_PLUS);

                    case AGE_P: applicableAgeLimits.add(Age_Limit.AGE_P);
                        break;
                }
            }

            Pageable pageable = PageRequest.of(page, pageSize);
            Page<MovieDTO> moviesPage;

            if (normalizedKeyword != null && genreId != null) {
                moviesPage = movieService.findMoviesByTheaterAndGenreAndKeywordAndDateRange(theaterId, genreId, normalizedKeyword, Movie_Status.Active, Theater_Status.Active, startDate, endDate, applicableAgeLimits, pageable);
            } else if (normalizedKeyword != null) {
                moviesPage = movieService.findMoviesByTheaterAndKeywordAndDateRange(theaterId, normalizedKeyword, Movie_Status.Active, Theater_Status.Active, startDate, endDate, applicableAgeLimits, pageable);
            } else if (genreId != null) {
                moviesPage = movieService.findMoviesByTheaterAndGenreAndDateRange(theaterId, genreId, Movie_Status.Active, Theater_Status.Active, startDate, endDate, applicableAgeLimits, pageable);
            } else {
                moviesPage = movieService.findMoviesByTheaterAndDateRange(theaterId, Movie_Status.Active, Theater_Status.Active, startDate, endDate, applicableAgeLimits, pageable);
            }

            List<GenreDTO> availableGenres = genreService.getAllGenres();
            String selectedGenreName = genreId != null ? availableGenres.stream().filter(g -> genreId.equals(g.getGenreID())).map(GenreDTO::getGenreName).findFirst().orElse(null) : null;

            List<LocalDateTime> availableDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                availableDates.add(LocalDateTime.now().plusDays(i).toLocalDate().atStartOfDay());
            }

            model.addAttribute("movies", moviesPage.getContent());
            model.addAttribute("moviesPage", moviesPage);
            model.addAttribute("keyword", normalizedKeyword);
            model.addAttribute("availableGenres", availableGenres);
            model.addAttribute("availableDates", availableDates);
            model.addAttribute("availableAgeLimits", Age_Limit.values());
            model.addAttribute("selectedGenreId", genreId);
            model.addAttribute("selectedDate", date);
            model.addAttribute("selectedAgeLimit", ageLimit);
            model.addAttribute("currentStep", 1);
            model.addAttribute("totalMovies", moviesPage.getTotalElements());
            model.addAttribute("hasSearchCriteria", (normalizedKeyword != null && !normalizedKeyword.isEmpty()) || genreId != null || date != null || ageLimit != null);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error to load film, pls reload page");
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

            List<LocalDateTime> availableDates = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                availableDates.add(LocalDateTime.now().plusDays(i).toLocalDate().atStartOfDay());
            }

            Page<ScheduleDTO> schedules = scheduleService.getSchedulesByMovieIdAndDate(theaterId, movieID, currentDate, sevenDate, Pageable.unpaged());

            for (ScheduleDTO schedule : schedules.getContent()) {
                List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(schedule.getRoomID()).getContent();
                List<Integer> bookedSeatIds = detailSeatService.findBookedSeatIdsByScheduleId(schedule.getScheduleID());
                SeatAvailabilityDTO availability = new SeatAvailabilityDTO();
                int totalSeats = allSeatsInRoom.size();
                int totalVipSeats = (int) allSeatsInRoom.stream().filter(SeatDTO::getIsVIP).count();
                int bookedVipSeats = (int) allSeatsInRoom.stream().filter(seat -> bookedSeatIds.contains(seat.getSeatID()) && seat.getIsVIP()).count();

                availability.setTotalSeats(totalSeats);
                availability.setAvailableSeats(totalSeats - bookedSeatIds.size());
                availability.setTotalVipSeats(totalVipSeats);
                availability.setAvailableVipSeats(totalVipSeats - bookedVipSeats);
                availability.setTotalRegularSeats(totalSeats - totalVipSeats);
                availability.setAvailableRegularSeats((totalSeats - totalVipSeats) - (bookedSeatIds.size() - bookedVipSeats));
                schedule.setSeatAvailability(availability);
            }

            session.setAttribute("selectedMovie", selectedMovie);
            model.addAttribute("currentStep", 2);
            model.addAttribute("schedules", schedules);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("availableDates", availableDates);
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

            List<SeatDTO> allSeatsInRoom = seatService.getSeatsByRoomId(selectedSchedule.getRoomID()).getContent();
            List<Integer> bookedSeatIds = detailSeatService.findBookedSeatIdsByScheduleId(scheduleId);

            Map<String, List<SeatDTO>> seatsByRow = allSeatsInRoom.stream()
                    .sorted(Comparator.comparing(s -> Integer.parseInt(s.getPosition().substring(1))))
                    .collect(Collectors.groupingBy(s -> s.getPosition().substring(0, 1), LinkedHashMap::new, Collectors.toList()));

            session.setAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("currentStep", 3);
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
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
            redirectAttributes.addFlashAttribute("errorMessage", "Session is expried");
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