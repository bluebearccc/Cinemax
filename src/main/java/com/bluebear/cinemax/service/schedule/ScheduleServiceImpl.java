package com.bluebear.cinemax.service.schedule;

import com.bluebear.cinemax.repository.DetailSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.enumtype.Schedule_Status;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private DetailSeatRepository detailSeatRepository;

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
            schedule.setMovie(optionalMovie.orElse(null));
            schedule.setRoom(optionalRoom.orElse(null));
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

    public Page<ScheduleDTO> getScheduleByMovieIdAndDate(Integer movieID, LocalDateTime date) {
        return scheduleRepository.findSchedulesByMovie_MovieIDInTodayAndStatus(movieID, date, Schedule_Status.Active, Pageable.unpaged()).map(this::toDTO);
    }

    public Page<ScheduleDTO> getScheduleByMovieIdAndTheaterIdAndDateAndRoomType(Integer movieID, Integer theaterID, LocalDateTime date, String roomType) {
        return scheduleRepository.findSchedulesByMovie_MovieIDAndTheaterAndDayAndRoomTypeStatus(movieID, theaterID, date, Schedule_Status.Active, roomType, Pageable.unpaged()).map(this::toDTO);
    }

    public Page<ScheduleDTO> getAllSchedules() {
        return scheduleRepository.findByStatus(Schedule_Status.Active, Pageable.unpaged())
                .map(this::toDTO);
    }

    public void calculateNumOfSeatLeft(ScheduleDTO scheduleDTO) {
        Room room = roomRepository.findById(scheduleDTO.getRoomID()).orElse(null);
        if (room != null) {
            int totalSeat = room.getRow() * room.getCollumn();
            int numOfSeatLeft = totalSeat - (int) detailSeatRepository.countBySchedule_ScheduleID(scheduleDTO.getScheduleID());
            scheduleDTO.setNumberOfSeatsRemain(numOfSeatLeft < 0 ? 0 : numOfSeatLeft);
        }
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

