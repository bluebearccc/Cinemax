package com.bluebear.cinemax.controller.staff;

import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Schedule;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movie_schedule")
public class MovieScheduleController {

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
        List<Movie> movies = movieServiceImpl.findAllShowingMovies();
        model.addAttribute("movies", movies);
        return "staff/movie_schedule";
    }

    @GetMapping("/search")
    public String searchMovieByName(@RequestParam("name") String name, Model model) {
        List<Movie> movies = movieServiceImpl.searchMovieByName(name);
        model.addAttribute("movies", movies);
        return "staff/movie_schedule";
    }

    @GetMapping("/show_schedule")
    public String showSchedule(@RequestParam("movieId") Integer movieID, Model model) {
        Employee e = employeeServiceImpl.getEmployeeById(2);
        List<Room> rooms = roomServiceImpl.findAllRoomsByTheaterId(e.getTheater().getTheaterId());
        Movie movie = movieServiceImpl.getMovieById(movieID);
        List<Schedule> schedules = scheduleServiceImpl.findByMovieId(movieID);
        model.addAttribute("movie", movie);
        model.addAttribute("startTime", movie.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("endTime", movie.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("schedules", convertSchedulesToJsonString(schedules));
        model.addAttribute("rooms", rooms);
        model.addAttribute("newSchedule", new Schedule());
        return "staff/movie_schedule_detail";
    }

    @PostMapping("/add_schedule")
    public String addNewSchedule(@RequestParam("movieId") Integer movieID,
                                 @RequestParam("roomId") Integer roomID,
                                 @RequestParam("date") String dateStr,
                                 @RequestParam("startTime") String startTimeStr,
                                 Model model) {

        LocalTime startTime = LocalTime.parse(startTimeStr);
        if (startTime.getHour() < 8) {
            model.addAttribute("error", "Movie schedule must start from 8 hour o clock");
        }

        LocalDate scheduleDate = LocalDate.parse(dateStr);
        Movie movie = movieServiceImpl.getMovieById(movieID);

        LocalDateTime actualStartTime = LocalDateTime.of(scheduleDate, startTime);
        LocalDateTime actualEndTime = actualStartTime.plusMinutes(movie.getDuration());

        LocalDateTime checkStartTime = actualStartTime.minusMinutes(15);
        LocalDateTime checkEndTime = actualEndTime.plusMinutes(15);

        List<Schedule> conflictingSchedules = scheduleServiceImpl.findOverlappingSchedules(roomID, checkStartTime, checkEndTime);

        if (!conflictingSchedules.isEmpty()) {
            model.addAttribute("error", "Conflic schedule in the same room");
            model.addAttribute("conflicts", conflictingSchedules);
        }

        Schedule newSchedule = new Schedule();
        newSchedule.setMovie(movie);
        newSchedule.setRoom(roomServiceImpl.getRoomById(roomID));
        newSchedule.setStartTime(actualStartTime);
        newSchedule.setEndTime(actualEndTime);
        newSchedule.setStatus("Active");

        scheduleServiceImpl.saveSchedule(newSchedule);
         return null;
    }

    private String convertSchedulesToJsonString(List<Schedule> schedules) {
        Map<String, List<Map<String, String>>> schedulesGroupedByDate = new java.util.HashMap<>();
        for (Schedule schedule : schedules) {
            String dateKey = schedule.getShowDateKey();
            List<Map<String, String>> schedulesForDay;
            if (schedulesGroupedByDate.containsKey(dateKey)) {
                schedulesForDay = schedulesGroupedByDate.get(dateKey);
            } else {
                 schedulesForDay = new java.util.ArrayList<>();
                 schedulesGroupedByDate.put(dateKey, schedulesForDay);
            }
            Map<String, String> scheduleInfo = new java.util.HashMap<>();
            scheduleInfo.put("time", schedule.getFormattedStartTime());
            scheduleInfo.put("endTime", schedule.getFormattedEndTime());
            scheduleInfo.put("room", schedule.getRoom().getName());
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

    @GetMapping("/schedule_fragment")
    public String showAddFrom(@RequestParam("movieId") Integer movieId ,@RequestParam("roomId") Integer roomId,@RequestParam("date") String date_raw, Model model) {
        LocalDate date = LocalDate.parse(date_raw);
        List<Schedule> schedules = scheduleServiceImpl.findAllScheduleByMovieIdAndRoomIdAndDate(movieId, roomId, date);
        model.addAttribute("schedules", schedules);
       return "staff/schedule_fragment :: schedule_fragment";
    }

}
