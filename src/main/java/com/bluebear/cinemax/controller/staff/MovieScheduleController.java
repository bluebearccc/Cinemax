package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import com.bluebear.cinemax.service.employee.EmployeeService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.movie.MovieService;
import com.bluebear.cinemax.service.room.RoomService;
import com.bluebear.cinemax.service.schedule.ScheduleService;
import com.bluebear.cinemax.service.theater.TheaterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/staff/movie_schedule")
public class MovieScheduleController {

    @Autowired
    TheaterService theaterServiceImpl;

    @Autowired
    EmployeeService employeeServiceImpl;

    @Autowired
    RoomService roomServiceImpl;

    @Autowired
    ScheduleService scheduleServiceImpl;

    @Autowired
    MovieService movieServiceImpl;

    @Autowired
    GenreService genreServiceImpl;

    @GetMapping
    public String getMovieSchedulePage(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "genre", required = false) Integer genreId,
            @RequestParam(value = "start_date", required = false) String startDate,
            @RequestParam(value = "end_date", required = false) String endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session,
            Model model) {

        Page<MovieDTO> moviePage = movieServiceImpl.findShowingMoviesWithFilters(name, genreId, startDate, endDate, page, size);
        Object employeeObj = session.getAttribute("employee");
        EmployeeDTO employee = (EmployeeDTO) employeeObj;
        model.addAttribute("employeeName", employee.getFullName());
        model.addAttribute("movies", moviePage);
        model.addAttribute("genres", genreServiceImpl.getAllGenres());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", moviePage.getTotalPages());

        model.addAttribute("name", name);
        model.addAttribute("genre", genreId);
        model.addAttribute("start_date", startDate);
        model.addAttribute("end_date", endDate);
        return "staff/movie_schedule";
    }
    @GetMapping("/show_schedule")
    public String showSchedule(@RequestParam("movieId") Integer movieID, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object employeeObj = session.getAttribute("employee");
        EmployeeDTO e = (EmployeeDTO) employeeObj;
        List<RoomDTO> rooms = roomServiceImpl.findAllRoomsByTheaterId(e.getTheaterId());
        MovieDTO movie = movieServiceImpl.getMovieById(movieID).get();
        List<ScheduleDTO> schedules = scheduleServiceImpl.findByMovieId(movieID);
        List<TheaterDTO> theaters = theaterServiceImpl.findAllTheaters();
        if (movie == null) {
            redirectAttributes.addFlashAttribute("message", "Error: Movie not found with ID " + movieID);
            return "redirect:/staff/movie_schedule";
        }
        model.addAttribute("name", e.getFullName());
        model.addAttribute("movie", movie);
        model.addAttribute("startTime", movie.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("endTime", movie.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("schedules", convertSchedulesToJsonString(schedules));
        model.addAttribute("rooms", rooms);
        model.addAttribute("newSchedule", new Schedule());
        model.addAttribute("theatersCbBox", theaters);
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

        MovieDTO mo = movieServiceImpl.getMovieById(movieId).get();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);

        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movieServiceImpl.getMovieById(movieId).get().getDuration());

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
    @GetMapping("/get_schedule_details")
    @ResponseBody
    public Map<String, Object> getScheduleDetails(@RequestParam("scheduleID") Integer scheduleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            ScheduleDTO schedule = scheduleServiceImpl.getScheduleById(scheduleId);
            if (schedule != null) {
                boolean isExistedInDetailSeat = scheduleServiceImpl.isExisted(scheduleId);
                response.put("isExisted", isExistedInDetailSeat);

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
                response.put("theaterList", theaterServiceImpl.findAllTheaters());
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Schedule not found.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching schedule details: " + e.getMessage());
        }
        return response;
    }
    @PostMapping("/update_schedule")
    public String updateSchedule(@RequestParam(value = "scheduleID") Integer scheduleId,
                                 @RequestParam(value = "movieID") Integer movieId,
                                 @RequestParam(value = "roomId", required = false) Integer roomId, // Đổi tên từ lần trước
                                 @RequestParam(value = "startTimeUpdate") String startTime_raw,
                                 @RequestParam(value = "dateUpdate") String date_raw,
                                 @RequestParam(value = "theaterUpdate") Integer theaterId,
                                 @RequestParam(value = "statusUpdate") String status,
                                 RedirectAttributes redirectAttributes) {

        if (scheduleServiceImpl.isExisted(scheduleId)) {
            redirectAttributes.addFlashAttribute("message", "Cannot edit this schedule. It has associated bookings.");
            return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
        }

        if (roomId == null) {
            redirectAttributes.addFlashAttribute("message", "Please select a room!");
            return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);
        MovieDTO movie = movieServiceImpl.getMovieById(movieId).get();
        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movie.getDuration());

        ScheduleDTO conflictingSchedule = scheduleServiceImpl.isRoomAvailableForUpdate(roomId, actualStartTime, actualEndTime, scheduleId);

        if (conflictingSchedule != null) {
            // Nếu có xung đột, xây dựng thông báo lỗi chi tiết
            String errorMessage = String.format(
                    "The selected room '%s' in theater '%s' is unavailable. It is already scheduled for the movie '%s' from %s to %s.",
                    conflictingSchedule.getRoomName(),
                    conflictingSchedule.getTheaterName(),
                    conflictingSchedule.getMovieName(),
                    conflictingSchedule.getFormattedStartTime(), // Giả sử bạn có phương thức này trong DTO
                    conflictingSchedule.getFormattedEndTime() // Giả sử bạn có phương thức này trong DTO
            );
            redirectAttributes.addFlashAttribute("message", errorMessage);
            return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
        }
        // --- KẾT THÚC VALIDATION MỚI ---

        // --- Logic cập nhật (giữ nguyên) ---
        ScheduleDTO existingSchedule = scheduleServiceImpl.getScheduleById(scheduleId);
        if (existingSchedule == null) {
            redirectAttributes.addFlashAttribute("message", "Schedule not found for update!");
            return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
        }
        existingSchedule.setStartTime(actualStartTime);
        existingSchedule.setEndTime(actualEndTime);
        existingSchedule.setMovieID(movieId);
        existingSchedule.setRoomID(roomId);
        try {
            existingSchedule.setStatus(Schedule_Status.valueOf(status));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Invalid status value: " + status);
            return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
        }
        scheduleServiceImpl.saveSchedule(existingSchedule);

        redirectAttributes.addFlashAttribute("message", "Schedule updated successfully!");
        return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
    }
    @PostMapping("/update_schedule_ajax")
    @ResponseBody
    public Map<String, Object> updateScheduleAjax(@RequestParam(value = "scheduleID") Integer scheduleId,
                                                  @RequestParam(value = "movieID") Integer movieId,
                                                  @RequestParam(value = "roomId", required = false) Integer roomId,
                                                  @RequestParam(value = "startTimeUpdate") String startTime_raw,
                                                  @RequestParam(value = "dateUpdate") String date_raw,
                                                  @RequestParam(value = "theaterUpdate") Integer theaterId,
                                                  @RequestParam(value = "statusUpdate") String status) {

        Map<String, Object> response = new HashMap<>();

        if (scheduleServiceImpl.isExisted(scheduleId)) {
            response.put("success", false);
            response.put("message", "Cannot edit this schedule. It has associated bookings.");
            return response;
        }

        if (roomId == null) {
            response.put("success", false);
            response.put("message", "Please select a room!");
            return response;
        }

        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDate date = LocalDate.parse(date_raw, dateFormatter);
            LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);
            MovieDTO movie = movieServiceImpl.getMovieById(movieId).get();
            LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
            LocalDateTime actualEndTime = actualStartTime.plusMinutes(movie.getDuration());

            ScheduleDTO conflictingSchedule = scheduleServiceImpl.isRoomAvailableForUpdate(roomId, actualStartTime, actualEndTime, scheduleId);

            if (conflictingSchedule != null) {
                String errorMessage = String.format(
                        "The selected room '%s' in theater '%s' is unavailable. It is already scheduled for the movie '%s' from %s to %s.",
                        conflictingSchedule.getRoomName(),
                        conflictingSchedule.getTheaterName(),
                        conflictingSchedule.getMovieName(),
                        conflictingSchedule.getFormattedStartTime(),
                        conflictingSchedule.getFormattedEndTime()
                );
                response.put("success", false);
                response.put("message", errorMessage);
                return response;
            }

            ScheduleDTO existingSchedule = scheduleServiceImpl.getScheduleById(scheduleId);
            if (existingSchedule == null) {
                response.put("success", false);
                response.put("message", "Schedule not found for update!");
                return response;
            }
            existingSchedule.setStartTime(actualStartTime);
            existingSchedule.setEndTime(actualEndTime);
            existingSchedule.setMovieID(movieId);
            existingSchedule.setRoomID(roomId);
            existingSchedule.setStatus(Schedule_Status.valueOf(status));

            scheduleServiceImpl.saveSchedule(existingSchedule);

            response.put("success", true);
            response.put("message", "Schedule updated successfully!");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }
    @GetMapping("/get_rooms_for_edit")
    public String getRoomsForEditFragment(@RequestParam("theaterId") Integer theaterId,
                                          @RequestParam("startTime") String startTime_raw,
                                          @RequestParam("date") String date_raw,
                                          @RequestParam("movieId") Integer movieId,
                                          @RequestParam("scheduleId") Integer scheduleId,
                                          Model model) {

        List<RoomDTO> allRoomsInTheater = roomServiceImpl.findAllRoomsByTheaterId(theaterId);
        model.addAttribute("availableRooms", allRoomsInTheater);

        ScheduleDTO currentSchedule = scheduleServiceImpl.getScheduleById(scheduleId);
        if (currentSchedule != null) {
            model.addAttribute("selectedRoomId", currentSchedule.getRoomID());
        }

        return "staff/fragments/available_rooms_fragment_update :: editRoomsFragment";
    }
    @PostMapping("/add_schedule")
    public String saveSchedule(@RequestParam(value = "movieId") Integer movieId,
                               @RequestParam(value = "selectedRoomIds") List<String> roomId,
                               @RequestParam(value = "startTime") String startTime_raw,
                               @RequestParam(value = "date") String date_raw,
                               RedirectAttributes redirectAttributes) {
        RoomDTO room = new RoomDTO();
        MovieDTO movie = movieServiceImpl.getMovieById(movieId).get();

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
        return "redirect:/staff/movie_schedule/show_schedule?movieId=" + movieId;
    }

    @PostMapping("/delete_schedule")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteSchedule(@RequestParam("scheduleId") Integer scheduleId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean result = scheduleServiceImpl.deleteSchedule(scheduleId);
            if (result) {
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("message", "Cannot delete schedule because it has existing bookings.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error occurred while deleting schedule: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}