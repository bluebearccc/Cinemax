package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.cashier.CustomerDTO;
import com.bluebear.cinemax.dto.cashier.MovieDTO;
import com.bluebear.cinemax.dto.cashier.SeatDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.service.cashier.CashierService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cashier")
public class CashierController {
    private LocalDate currentDate = LocalDate.now();

    private CashierService cashierService;

    @Autowired
    public CashierController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("")
    public String index(Model model, HttpSession session) {
        // Reset session
        session.removeAttribute("selectedMovie");
        session.removeAttribute("selectedSchedule");
        session.removeAttribute("selectedSeats");
        session.removeAttribute("customerInfo");
        session.removeAttribute("priceBreakdown");
        session.removeAttribute("currentStep");

        model.addAttribute("movies", cashierService.getMovieAvailable(Movie.MovieStatus.Active, currentDate));
        model.addAttribute("currentStep", 1);
        return "cashier-templates/cashier-booking";
    }

    @GetMapping("/{id}")
    public String selectMovie(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            var selectedMovie = cashierService.getMovieById(id);

            if (selectedMovie == null) {
                return "redirect:/cashier/";
            }

            // Clear subsequent session data when going back to movie selection
            session.removeAttribute("selectedSchedule");
            session.removeAttribute("selectedSeats");
            session.removeAttribute("customerInfo");
            session.removeAttribute("priceBreakdown");

            // Set movie and step
            session.setAttribute("selectedMovie", selectedMovie);
            session.setAttribute("currentStep", 2);

            // Add to model for display
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("currentStep", 2);
            model.addAttribute("schedules", cashierService.getAllSchedulesByMovieIdAndDate(id, currentDate));
            model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            return "cashier-templates/cashier-booking";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/";
        }
    }

    @PostMapping("/select-schedule")
    public String selectSchedule(@RequestParam String time,
                                 @RequestParam String roomName,
                                 @RequestParam String roomType,
                                 @RequestParam Integer scheduleId,
                                 Model model, HttpSession session) {
        try {
            var selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");

            if (selectedMovie == null) {
                return "redirect:/cashier/";
            }

            session.removeAttribute("selectedSeats");
            session.removeAttribute("customerInfo");
            session.removeAttribute("priceBreakdown");

            Map<String, Object> selectedSchedule = new HashMap<>();
            selectedSchedule.put("time", time);
            selectedSchedule.put("roomName", roomName);
            selectedSchedule.put("roomType", roomType);
            selectedSchedule.put("scheduleId", scheduleId);

            session.setAttribute("selectedSchedule", selectedSchedule);
            session.setAttribute("currentStep", 3);

            List<SeatDTO> availableSeats = cashierService.getAvailableSeatsByScheduleId(scheduleId);

            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("currentStep", 3);
            model.addAttribute("availableSeats", availableSeats); // Thay vì seatMap

            return "cashier-templates/cashier-booking";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/";
        }
    }

    @GetMapping("/back-to-seats")
    public String backToSeats(Model model, HttpSession session) {
        var selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
        var selectedSchedule = (Map<String, Object>) session.getAttribute("selectedSchedule");

        if (selectedMovie == null || selectedSchedule == null) {
            return "redirect:/cashier/";
        }

        session.removeAttribute("selectedSeats");
        session.removeAttribute("customerInfo");
        session.removeAttribute("priceBreakdown");
        session.setAttribute("currentStep", 3);

        Integer scheduleId = (Integer) selectedSchedule.get("scheduleId");

        // Đơn giản hóa - chỉ lấy danh sách ghế trống
        List<SeatDTO> availableSeats = cashierService.getAvailableSeatsByScheduleId(scheduleId);

        model.addAttribute("selectedMovie", selectedMovie);
        model.addAttribute("selectedSchedule", selectedSchedule);
        model.addAttribute("currentStep", 3);
        model.addAttribute("availableSeats", availableSeats); // Thay vì seatMap

        return "cashier-templates/cashier-booking";
    }

    @PostMapping("/select-seats")
    public String selectSeats(@RequestParam("selectedSeats") String[] selectedSeats,
                              Model model, HttpSession session) {
        try {
            var selectedMovie = (MovieDTO) session.getAttribute("selectedMovie");
            var selectedSchedule = (Map<String, Object>) session.getAttribute("selectedSchedule");

            if (selectedMovie == null || selectedSchedule == null) {
                return "redirect:/cashier/";
            }

            // Clear subsequent session data
            session.removeAttribute("customerInfo");
            session.removeAttribute("priceBreakdown");

            // Save selected seats
            session.setAttribute("selectedSeats", selectedSeats);
            session.setAttribute("currentStep", 4);

            // Calculate price breakdown
            String roomType = (String) selectedSchedule.get("roomType");
            Map<String, Object> priceBreakdown = calculatePriceBreakdown(selectedSeats, roomType);
            session.setAttribute("priceBreakdown", priceBreakdown);

            // Add to model for display
            model.addAttribute("selectedMovie", selectedMovie);
            model.addAttribute("selectedSchedule", selectedSchedule);
            model.addAttribute("selectedSeats", selectedSeats);
            model.addAttribute("currentStep", 4);
            model.addAttribute("priceBreakdown", priceBreakdown);
            model.addAttribute("foodMenu", getFoodMenu());

            return "cashier-templates/cashier-booking";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/cashier/";
        }
    }

    private float calculateDiscount(String promotionId, int totalAmount) {
        if (promotionId == null || promotionId.isEmpty()) {
            return 0f;
        }

        switch (promotionId) {
            case "1": return 10f; // VIP discount
            case "2": return 5f;  // Student discount
            case "3": return 15f; // Special combo
            default: return 0f;
        }
    }

    private Map<String, Object> calculatePriceBreakdown(String[] seats, String roomType) {
        Map<String, Object> breakdown = new HashMap<>();
        List<Map<String, Object>> ticketItems = new ArrayList<>();

        int totalTicketPrice = 0;

        for (String seat : seats) {
            int seatPrice = calculateSeatPrice(seat, roomType);
            totalTicketPrice += seatPrice;

            Map<String, Object> ticketItem = new HashMap<>();
            ticketItem.put("description", "Ghế " + seat);
            ticketItem.put("price", seatPrice);
            ticketItems.add(ticketItem);
        }

        breakdown.put("ticketItems", ticketItems);
        breakdown.put("foodItems", new ArrayList<>()); // Empty for now
        breakdown.put("total", totalTicketPrice);

        return breakdown;
    }

    private int calculateSeatPrice(String seatId, String roomType) {
        if ("couple".equals(roomType)) {
            return 190000;
        } else {
            // Check if VIP (rows E-H)
            char rowChar = seatId.charAt(0);
            return (rowChar >= 'E') ? 95000 : 75000;
        }
    }

    private List<Map<String, Object>> getFoodMenu() {
        List<Map<String, Object>> menu = new ArrayList<>();

        menu.add(createFoodItem(1, "Bắp rang bơ lớn", 65000));
        menu.add(createFoodItem(2, "Bắp rang bơ vừa", 45000));
        menu.add(createFoodItem(3, "Coca Cola lớn", 35000));
        menu.add(createFoodItem(4, "Coca Cola vừa", 25000));
        menu.add(createFoodItem(5, "Combo 1 (Bắp lớn + Nước lớn)", 85000));
        menu.add(createFoodItem(6, "Combo 2 (Bắp vừa + Nước vừa)", 65000));

        return menu;
    }

    private Map<String, Object> createFoodItem(int id, String name, int price) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("price", price);
        return item;
    }
}