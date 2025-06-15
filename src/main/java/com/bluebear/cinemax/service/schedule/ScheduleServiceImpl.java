package com.bluebear.cinemax.service.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.ScheduleRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private RoomRepository roomRepository;

    public ScheduleDTO createSchedule(ScheduleDTO dto) {
        Schedule schedule = toEntity(dto);
        return toDTO(scheduleRepository.save(schedule));
    }


    public ScheduleDTO updateSchedule(ScheduleDTO dto) {
        Optional<Schedule> optional = scheduleRepository.findById(dto.getScheduleID());
        Optional<Movie> optionalMovie = movieRepository.findById(dto.getMovieID());
        Optional<Room> optionalRoom = roomRepository.findById(dto.getRoomID());

        if (optional.isPresent()) {
            Schedule schedule = optional.get();
            schedule.setScheduleID(dto.getScheduleID());
            schedule.setMovie(optionalMovie.get());
            schedule.setRoom(optionalRoom.get());
            schedule.setStatus(dto.getStatus());
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            return toDTO(scheduleRepository.save(schedule));
        }
        return null;
    }


    public void deleteSchedule(Integer scheduleID) {
        scheduleRepository.deleteById(scheduleID);
    }


    public ScheduleDTO getScheduleById(Integer scheduleID) {
        Optional<Schedule> optional = scheduleRepository.findById(scheduleID);
        return optional.map(this::toDTO).orElse(null);
    }

    public List<ScheduleDTO> getScheduleByMovieIdAndDate(Integer movieID, Date date) {
        return scheduleRepository.findSchedulesByMovie_MovieIDInToday(movieID, date).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findByStatus(Schedule_Status.Active)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleID(schedule.getScheduleID());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMovieID(schedule.getMovie().getMovieID());
        dto.setRoomID(schedule.getRoom().getRoomID());
        dto.setStatus(schedule.getStatus());
        return dto;
    }

    public Schedule toEntity(ScheduleDTO dto) {
        Schedule schedule = new Schedule();
        schedule.setScheduleID(dto.getScheduleID());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());

        Movie movie = movieRepository.findById(dto.getMovieID()).orElse(null);
        Room room = roomRepository.findById(dto.getRoomID()).orElse(null);

        schedule.setMovie(movie);
        schedule.setRoom(room);
        schedule.setStatus(dto.getStatus());
        return schedule;
    }
}
