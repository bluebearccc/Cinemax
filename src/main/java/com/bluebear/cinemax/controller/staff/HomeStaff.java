package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.dto.RevenueDataDTO;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.service.detail_fd.DetaillFD_Service;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.theater.TheaterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeStaff {
    @Autowired
    private DetaillFD_Service detaillFDService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private ScheduleService scheduleService;
    @GetMapping("/staff/home")
    public String staffHome(HttpSession session, Model model) {
        Object employeeObj = session.getAttribute("employee");
        if (employeeObj == null) {
            return "redirect:/login";
        }
        EmployeeDTO employee = (EmployeeDTO) employeeObj;
        model.addAttribute("name", employee.getFullName());

        List<TheaterDTO> theaters = theaterService.findAllTheaters();
        model.addAttribute("theaters", theaters);
        Integer defaultTheaterId = theaters.get(0).getTheaterID();
        model.addAttribute("selectedTheaterId", defaultTheaterId);
        return "staff/home";
    }

    @GetMapping("/revenue/food")
    @ResponseBody
    public ResponseEntity<RevenueDataDTO> getRevenueByItem(
            @RequestParam Integer theaterId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            RevenueDataDTO revenueData = detaillFDService.getRevenueByItemForMonth(theaterId, year, month    );
            return ResponseEntity.ok(revenueData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/schedules")
    @ResponseBody
    public ResponseEntity<List<ScheduleDTO>> getSchedules(
            @RequestParam("theaterId") Integer theaterId,
                @RequestParam("date")  LocalDate date) {
            List<ScheduleDTO> schedules = scheduleService.findSchedulesByTheaterAndDate(theaterId, date);
        if (schedules == null || schedules.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(schedules);
    }
    @GetMapping("/staff/logout")
    public String logout(HttpSession session) {
        return "redirect:/logout";
    }
}
