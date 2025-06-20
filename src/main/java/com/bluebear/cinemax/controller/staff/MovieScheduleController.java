package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import com.bluebear.cinemax.service.staff.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/movie_schedule")
public class MovieScheduleController {

    @Autowired
    TheaterServiceImpl theaterServiceImpl;

    @Autowired
    EmployeeServiceImpl employeeServiceImpl;

    @Autowired
    RoomServiceImpl roomServiceImpl;

    @Autowired
    ScheduleServiceImpl scheduleServiceImpl;

    @Autowired
    MovieServiceImpl movieServiceImpl;

    @GetMapping
    public String getMovieSchedulePage(Model model) {

        List<MovieDTO> movies = movieServiceImpl.findAllShowingMovies();

        model.addAttribute("movies", movies);

        return "staff/movie_schedule";
    }

    @GetMapping("/search")
    public String searchMovieByName(@RequestParam("name") String name, Model model) {

        List<MovieDTO> movies = movieServiceImpl.searchExistingMovieByName(name);

        model.addAttribute("movies", movies);

        return "staff/movie_schedule";
    }

    @GetMapping("/show_schedule")
    public String showSchedule(@RequestParam("movieId") Integer movieID, Model model) {
        EmployeeDTO e = employeeServiceImpl.getEmployeeById(4);

        List<RoomDTO> rooms = roomServiceImpl.findAllRoomsByTheaterId(e.getTheaterId());

        MovieDTO movie = movieServiceImpl.getMovieById(movieID);

        List<ScheduleDTO> schedules = scheduleServiceImpl.findByMovieId(movieID);

        List<TheaterDTO> theaters = theaterServiceImpl.findAllTheaters();

        model.addAttribute("movie", movie);
        model.addAttribute("startTime", movie.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("endTime", movie.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("schedules", convertSchedulesToJsonString(schedules));
        model.addAttribute("rooms", rooms);
        model.addAttribute("newSchedule", new Schedule());
        model.addAttribute("theatersCbBox", theaters);

        // Thêm tin nhắn từ redirectAttributes (nếu có)
        if (model.asMap().containsKey("message")) {
            model.addAttribute("message", model.asMap().get("message"));
        }


        return "staff/movie_schedule_detail";
    }

    private String convertSchedulesToJsonString(List<ScheduleDTO> schedules) {
        Map<String, List<Map<String, String>>> schedulesGroupedByDate = new java.util.HashMap<>();
        for (ScheduleDTO schedule : schedules) {
            String dateKey = schedule.getShowDateKey();
            List<Map<String, String>> schedulesForDay;
            if (schedulesGroupedByDate.containsKey(dateKey)) {
                schedulesForDay = schedulesGroupedByDate.get(dateKey);
            } else {
                schedulesForDay = new java.util.ArrayList<>();
                schedulesGroupedByDate.put(dateKey, schedulesForDay);
            }
            RoomDTO r = roomServiceImpl.getRoomById(schedule.getRoomID());
            TheaterDTO t = theaterServiceImpl.getTheaterById(r.getTheaterID());
            Map<String, String> scheduleInfo = new java.util.HashMap<>();
            scheduleInfo.put("id", String.valueOf(schedule.getScheduleID()));
            scheduleInfo.put("time", schedule.getFormattedStartTime());
            scheduleInfo.put("endTime", schedule.getFormattedEndTime());
            scheduleInfo.put("room", r.getName());
            scheduleInfo.put("theater", t.getTheaterName() );
            schedulesForDay.add(scheduleInfo);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(schedulesGroupedByDate);
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            return "{}";
        }
    }

    @GetMapping("/available_rooms_fragment")
    public String getAvailablerRoomsFragment(@RequestParam("theaterId") Integer theaterId,
                                             @RequestParam("startTime") String startTime_raw,
                                             @RequestParam("date") String date_raw,
                                             @RequestParam("movieId") Integer movieId,
                                             @RequestParam(value = "selectedRoomId", required = false) Integer selectedRoomId, // Thêm tham số này
                                             Model model) {

        List<String> availableRoomsId = new ArrayList<>();

        MovieDTO mo = movieServiceImpl.getMovieById(movieId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);

        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movieServiceImpl.getMovieById(movieId).getDuration());

        if(actualEndTime.isAfter(mo.getEndDate())) {
            String message = "Cannot add schedule because this movie is out of valid date range";
            model.addAttribute("message", message);
            return "staff/fragments/available_rooms_fragment :: availablerRooms";
        }

        LocalDateTime bufferStartTime = actualStartTime.minusMinutes(15);
        LocalDateTime bufferEndTime = actualEndTime.plusMinutes(15);

        availableRoomsId = scheduleServiceImpl.findAvailableRooms(theaterId, bufferStartTime, bufferEndTime);
        List<RoomDTO> availableRooms = new ArrayList();

        for(String roomId : availableRoomsId) {
            availableRooms.add(roomServiceImpl.getRoomById(Integer.parseInt(roomId)));
        }
        if(availableRooms.isEmpty()) {
            String message = "No available rooms for the selected time and theater";
            model.addAttribute("message", message);
            return "staff/fragments/available_rooms_fragment :: availablerRooms";
        }
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("selectedRoomId", selectedRoomId); // Truyền selectedRoomId vào model
        return "staff/fragments/available_rooms_fragment :: availablerRooms";
    }
    @PostMapping("/add_schedule")
    public String saveSchedule(@RequestParam(value = "movieId") Integer movieId,
                               @RequestParam(value = "selectedRoomIds") List<String> roomId,
                               @RequestParam(value = "startTime") String startTime_raw,
                               @RequestParam(value = "date") String date_raw,
                               RedirectAttributes redirectAttributes) {
        RoomDTO room = new RoomDTO();
        MovieDTO movie = movieServiceImpl.getMovieById(movieId);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);

        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movie.getDuration());

        for(String roomId_ : roomId) {
            ScheduleDTO schedule = new ScheduleDTO();
            schedule.setMovieID(movieId);
            schedule.setRoomID(Integer.parseInt(roomId_));
            schedule.setStartTime(actualStartTime);
            schedule.setEndTime(actualEndTime);
            schedule.setStatus(Schedule_Status.Active);
            scheduleServiceImpl.saveSchedule(schedule);
        }
        redirectAttributes.addFlashAttribute("message", "Schedule added successfully!");
        return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
    }

    @PostMapping("/delete_schedule") // Đổi từ GET sang POST
    @ResponseBody // Trả về JSON cho AJAX
    public Map<String, Object> deleteSchedule(@RequestParam(value ="scheduleId") Integer scheduleId){
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra xem lịch trình có tồn tại trong detailseat không
        if (scheduleServiceImpl.isExisted(scheduleId)) {
            response.put("success", false);
            response.put("message", "Cannot delete schedule. It has associated bookings.");
            return response;
        }

        // Nếu không có trong detailseat, tiến hành xóa
        if (scheduleServiceImpl.deleteSchedule(scheduleId)) {
            response.put("success", true);
            response.put("message", "Schedule deleted successfully!");
        } else {
            response.put("success", false);
            response.put("message", "Failed to delete schedule due to a server error.");
        }
        return response;
    }

    @GetMapping("/get_schedule_details")
    @ResponseBody // Đảm bảo trả về JSON
    public Map<String, Object> getScheduleDetails(@RequestParam("scheduleID") Integer scheduleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ScheduleDTO schedule = scheduleServiceImpl.getScheduleById(scheduleId);
            if (schedule != null) {
                // Thêm kiểm tra isExisted vào đây để disable button Edit/Delete trên frontend
                boolean isExistedInDetailSeat = scheduleServiceImpl.isExisted(scheduleId);
                response.put("isExisted", isExistedInDetailSeat); // Thêm trạng thái tồn tại

                RoomDTO room = roomServiceImpl.getRoomById(schedule.getRoomID());
                TheaterDTO theater = null;
                if (room != null) {
                    theater = theaterServiceImpl.getTheaterById(room.getTheaterID());
                }

                response.put("scheduleId", schedule.getScheduleID());
                response.put("movieId", schedule.getMovieID());
                response.put("date", schedule.getShowDateKey()); //yyyy-MM-dd
                response.put("startTime", schedule.getFormattedStartTime()); // HH:mm
                response.put("endTime", schedule.getFormattedEndTime());
                response.put("roomId", schedule.getRoomID());
                response.put("roomName", room != null ? room.getName() : ""); // Thêm roomName vào response
                response.put("theaterId", theater != null ? theater.getTheaterID() : "");
                response.put("theaterName", theater != null ? theater.getTheaterName() : "");
                response.put("status", schedule.getStatus().name()); // Thêm status vào response

                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Schedule not found.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching schedule details: " + e.getMessage());
            // Log lỗi e.printStackTrace();
        }
        return response;
    }

    @PostMapping("/update_schedule")
    public String updateSchedule(@RequestParam(value = "scheduleID") Integer scheduleId,
                                 @RequestParam(value = "movieID") Integer movieId,
                                 @RequestParam(value = "selectedRoomIds", required = false) Integer roomId,
                                 @RequestParam(value = "startTimeUpdate") String startTime_raw,
                                 @RequestParam(value = "dateUpdate") String date_raw,
                                 @RequestParam(value = "theaterUpdate") Integer theaterId,
                                 @RequestParam(value = "statusUpdate") String status, // Thêm tham số status
                                 RedirectAttributes redirectAttributes) {

        // --- Bắt đầu kiểm tra isExisted ---
        if (scheduleServiceImpl.isExisted(scheduleId)) {
            redirectAttributes.addFlashAttribute("message", "Cannot edit this schedule. It has associated bookings.");
            return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
        }
        // --- Kết thúc kiểm tra isExisted ---

        ScheduleDTO existingSchedule = scheduleServiceImpl.getScheduleById(scheduleId);
        if (existingSchedule == null) {
            redirectAttributes.addFlashAttribute("message", "Schedule not found for update!");
            return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);

        MovieDTO movie = movieServiceImpl.getMovieById(movieId);
        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movie.getDuration());

        existingSchedule.setStartTime(actualStartTime);
        existingSchedule.setEndTime(actualEndTime);
        existingSchedule.setMovieID(movieId);

        if (roomId != null) {
            existingSchedule.setRoomID(roomId);
        } else {
            redirectAttributes.addFlashAttribute("message", "Please select a room!");
            return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
        }

        try {
            existingSchedule.setStatus(Schedule_Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Invalid status value: " + status);
            return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
        }

        scheduleServiceImpl.saveSchedule(existingSchedule);

        redirectAttributes.addFlashAttribute("message", "Schedule updated successfully!");
        return "redirect:/movie_schedule/show_schedule?movieId=" + movieId;
    }
}