package com.bluebear.cinemax.service.schedule;

import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Override
    @Transactional
    public boolean deleteSchedule(Integer scheduleID) {
        try {
            if (isExisted(scheduleID)) {
                return false;
            }

            int deletedRows = scheduleRepository.deleteByScheduleId(scheduleID);
            return deletedRows > 0;
        } catch (Exception e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            return false;
        }
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
        dto.setMovieName(schedule.getMovie().getMovieName());
        if (schedule.getRoom() != null) {
            dto.setRoomName(schedule.getRoom().getName());
            dto.setRoomType(schedule.getRoom().getTypeOfRoom().name());
        }
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

    @Override
    public List<ScheduleDTO> findByMovieId(Integer movieId) {
        List<Schedule> schedules = scheduleRepository.findByMovie_MovieID(movieId);
        return schedules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDTO> findAllScheduleByMovieIdAndRoomIdAndDate(Integer movieId, Integer roomId, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.findAllByMovieIdAndRoomId(movieId, roomId, date);
        return schedules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveSchedule(ScheduleDTO scheduleDTO) {
        Schedule scheduleToSave = toEntity(scheduleDTO);
        Schedule savedSchedule = scheduleRepository.save(scheduleToSave);
    }

    public ScheduleDTO getScheduleById(Integer id) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        return scheduleOptional.map(this::toDTO).orElse(null);
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
            return toDTO(updatedSchedule);
        }
        return null;
    }

    @Override
    public boolean isExisted(Integer scheduleId) {
        List<Schedule> schedules = scheduleRepository.findSchedulesByDetailSeat(scheduleId);
        if(schedules.size() > 0){
            return true;
        }
        return false;
    }

    @Override
    public ScheduleDTO isRoomAvailableForUpdate(Integer roomId, LocalDateTime startTime, LocalDateTime endTime, Integer scheduleId) {
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(roomId, startTime, endTime, scheduleId);

        // 2. Nếu danh sách rỗng, tức là không có xung đột -> phòng trống
        if (conflictingSchedules.isEmpty()) {
            return null; // Trả về null để báo hiệu phòng trống
        }

        // 3. Nếu có xung đột, lấy lịch trình đầu tiên và tạo một DTO chứa đầy đủ thông tin chi tiết
        Schedule conflict = conflictingSchedules.get(0);
        Room room = conflict.getRoom();
        Movie movie = conflict.getMovie();
        Theater theater = (room != null) ? room.getTheater() : null;

        // Xây dựng một DTO với đầy đủ thông tin để hiển thị cho người dùng
        return ScheduleDTO.builder()
                .scheduleID(conflict.getScheduleID())
                .startTime(conflict.getStartTime())
                .endTime(conflict.getEndTime())
                .roomName(room != null ? room.getName() : "N/A")
                .movieName(movie != null ? movie.getMovieName() : "N/A")
                .theaterName(theater != null ? theater.getTheaterName() : "N/A")
                .build();
    }

    @Override
    public Page<ScheduleDTO> getSchedulesByMovieIdAndDate(Integer theaterId, Integer movieId,
                                                          LocalDateTime startDate, LocalDateTime endDate,
                                                          Pageable pageable) {
        Page<Schedule> schedulesPage = scheduleRepository.findSchedulesByMovieIdAndTheaterIdAndDateRange(
                theaterId, movieId, startDate, endDate, pageable);

        List<ScheduleDTO> scheduleDTOs = schedulesPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(scheduleDTOs, pageable, schedulesPage.getTotalElements());
    }

    public List<ScheduleDTO> findSchedulesByTheaterAndDate(Integer theaterId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Schedule> schedules = scheduleRepository.findSchedulesByTheaterAndDateRange(theaterId, startOfDay, endOfDay);
        return schedules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}

