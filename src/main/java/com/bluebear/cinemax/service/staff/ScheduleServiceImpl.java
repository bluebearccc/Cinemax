package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.ScheduleDTO;
import com.bluebear.cinemax.entity.Movie;
import com.bluebear.cinemax.entity.Room;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private ScheduleDTO convertToDTO(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        return ScheduleDTO.builder()
                .scheduleID(schedule.getScheduleID())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .movieID(schedule.getMovie() != null ? schedule.getMovie().getMovieID() : null)
                .roomID(schedule.getRoom() != null ? schedule.getRoom().getRoomID() : null)
                .status(schedule.getStatus())
                .build();
    }


    private Schedule convertToEntity(ScheduleDTO scheduleDTO) {
        if (scheduleDTO == null) {
            return null;
        }

        Schedule schedule = new Schedule();
        schedule.setScheduleID(scheduleDTO.getScheduleID());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setStatus(scheduleDTO.getStatus());

        if (scheduleDTO.getMovieID() != null) {
            movieRepository.findById(scheduleDTO.getMovieID())
                    .ifPresent(schedule::setMovie);
        }
        if (scheduleDTO.getRoomID() != null) {
            roomRepository.findById(scheduleDTO.getRoomID())
                    .ifPresent(schedule::setRoom);
        }

        return schedule;
    }

    @Override
    public List<ScheduleDTO> findByMovieId(Integer movieId) {
        List<Schedule> schedules = scheduleRepository.findByMovie_MovieID(movieId);
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDTO> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findAllByMovieIdAndRoomId(movieId, roomId, date);
        return schedules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveSchedule(ScheduleDTO scheduleDTO) {
        Schedule scheduleToSave = convertToEntity(scheduleDTO);
        Schedule savedSchedule = scheduleRepository.save(scheduleToSave);
    }

    public ScheduleDTO getScheduleById(Integer id) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        return scheduleOptional.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<String> findAvailableRooms(Integer theaterId, LocalDateTime startTime, LocalDateTime endTime) {
        return scheduleRepository.findAvailableRooms(theaterId, startTime, endTime);
    }

    public ScheduleDTO updateSchedule(Integer id, ScheduleDTO scheduleDTO) {
        Optional<Schedule> existingScheduleOptional = scheduleRepository.findById(id);
        if (existingScheduleOptional.isPresent()) {
            Schedule existingSchedule = existingScheduleOptional.get();

            existingSchedule.setStartTime(scheduleDTO.getStartTime());
            existingSchedule.setEndTime(scheduleDTO.getEndTime());
            existingSchedule.setStatus(scheduleDTO.getStatus());

            if (scheduleDTO.getMovieID() != null) {
                movieRepository.findById(scheduleDTO.getMovieID())
                        .ifPresent(existingSchedule::setMovie);
            } else {
                existingSchedule.setMovie(null);
            }

            if (scheduleDTO.getRoomID() != null) {
                roomRepository.findById(scheduleDTO.getRoomID())
                        .ifPresent(existingSchedule::setRoom);
            } else {
                existingSchedule.setRoom(null);
            }

            Schedule updatedSchedule = scheduleRepository.save(existingSchedule);
            return convertToDTO(updatedSchedule);
        }
        return null;
    }

    public boolean deleteSchedule(Integer id){
        try{
            scheduleRepository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException e) {
            System.err.println("Cannot delete Schedule with ID " + id + " due to data integrity violation: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Xử lý các loại lỗi khác
            System.err.println("An error occurred while deleting Schedule with ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    //nếu có tồn tại ở trong detailseat thi return true
    // nếu không thì  false
    @Override
    public boolean isExisted(Integer scheduleId) {
        List<Schedule> schedules = scheduleRepository.findSchedulesByDetailSeat(scheduleId);
        if(schedules.size() > 0){
            return true;
        }
        return false;
    }
}