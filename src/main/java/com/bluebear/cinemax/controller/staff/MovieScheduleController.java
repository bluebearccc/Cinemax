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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
                                             Model model) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(date_raw, dateFormatter);
        LocalTime startTime = LocalTime.parse(startTime_raw, timeFormatter);

        LocalDateTime actualStartTime = LocalDateTime.of(date, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movieServiceImpl.getMovieById(movieId).getDuration());

        LocalDateTime bufferStartTime = actualStartTime.minusMinutes(15);
        LocalDateTime bufferEndTime = actualEndTime.plusMinutes(15);

        List<String> availableRoomsId = scheduleServiceImpl.findAvailableRooms(theaterId, bufferStartTime, bufferEndTime);
        List<RoomDTO> availableRooms = new ArrayList();

        for(String roomId : availableRoomsId) {
            availableRooms.add(roomServiceImpl.getRoomById(Integer.parseInt(roomId)));
        }
        model.addAttribute("availableRooms", availableRooms);
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

    @GetMapping("/remove_schedule")
    public String removeSchedule(@RequestParam(value ="scheduleId") Integer scheduleId,
                                 RedirectAttributes redirectAttributes){
        String message = "Cannot delete because this schedule";
        if(scheduleServiceImpl.deleteSchedule(scheduleId)) {
            message = "Schedule deleted successfully!";
        }
        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/movie_schedule";
    }
}
