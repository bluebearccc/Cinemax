package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class TheaterServiceImpl implements TheaterService {

    private static final String UPLOAD_DIR = "D:\\SWP391\\Cinemax\\uploads\\images";

    @Autowired
    private TheaterRepository theaterRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomService roomService;

    public TheaterDTO createTheater(TheaterDTO dto) {
        Theater theater = toEntity(dto);
        return toDTO(theaterRepository.save(theater));
    }

    public TheaterDTO updateTheater(Integer id, TheaterDTO dto) {
        Optional<Theater> optionalTheater = theaterRepository.findById(id);
        if (optionalTheater.isEmpty()) return null;

        Theater existing = optionalTheater.get();
        existing.setTheaterName(dto.getTheaterName());
        existing.setAddress(dto.getAddress());
        existing.setImage(dto.getImage());
        existing.setRoomQuantity(dto.getRoomQuantity());
        existing.setStatus(dto.getStatus());
        if (dto.getRooms() != null) {
            existing.setRooms(dto.getRooms().stream().map(roomDTO -> roomService.toEntity(roomDTO)).collect(Collectors.toList()));
        }

        return toDTO(theaterRepository.save(existing));
    }

    @Override
    public void deleteTheater(Integer id) {
        theaterRepository.deleteById(id);
    }

    public TheaterDTO getTheaterById(Integer id) {
        return theaterRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public TheaterDTO getTheaterByIdWithRateCounts(Integer id) {
        TheaterDTO theaterDTO = theaterRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);

        theaterDTO.setServiceRate(4.5);
        theaterDTO.setNumberOfRate(24);

        return theaterDTO;
    }

    public Page<TheaterDTO> getAllTheaters() {
        return theaterRepository.findByStatus(Theater_Status.Active, Pageable.unpaged()).map(this::toDTO);
    }

    @Override
    public List<TheaterDTO> findAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TheaterDTO> findAllTheaters(String status) {
        List<Theater> theaters;

        // Nếu status có giá trị và không phải "All"
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("All")) {
            try {
                // Chuyển đổi chuỗi status thành Enum
                Theater_Status statusEnum = Theater_Status.valueOf(status);
                // Gọi phương thức findByStatus từ repository
                theaters = theaterRepository.findByStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                // Nếu status không hợp lệ, trả về danh sách trống
                theaters = List.of();
            }
        } else {
            // Nếu không có filter, lấy tất cả
            theaters = theaterRepository.findAll();
        }

        return theaters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TheaterDTO> findAllTheatersByName(String keyword) {
        List<Theater> theaters = theaterRepository.findByTheaterNameContainingIgnoreCase(keyword);
        return theaters.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public TheaterDTO saveTheater(TheaterDTO theaterDTO) {
        Theater theaterToSave = toEntity(theaterDTO);
        Theater savedTheater = theaterRepository.save(theaterToSave);
        return toDTO(savedTheater);
    }

    @Override
    public TheaterDTO addTheater(TheaterDTO theaterDTO, MultipartFile imageFile) throws Exception {
        // Validation 1: Tên rạp phải bắt đầu bằng "CGV" (không phân biệt hoa/thường)
        if (!theaterDTO.getTheaterName().trim().toLowerCase().startsWith("cgv")) {
            throw new Exception("Tên rạp chiếu phải bắt đầu bằng 'CGV'.");
        }

        // Validation 2: Tên rạp không được trùng
        if (theaterRepository.existsByTheaterNameIgnoreCase(theaterDTO.getTheaterName().trim())) {
            throw new Exception("Tên rạp chiếu đã tồn tại. Vui lòng chọn tên khác.");
        }

        // Validation 3: Địa chỉ không được trùng
        if (theaterRepository.existsByAddressIgnoreCase(theaterDTO.getAddress().trim())) {
            throw new Exception("Địa chỉ rạp chiếu đã tồn tại. Vui lòng kiểm tra lại.");
        }

        Theater theater = toEntity(theaterDTO);

        // Xử lý upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            // Lưu đường dẫn tương đối để truy cập từ web
            theater.setImage("/uploads/theaters/" + fileName);
        }

        // Thiết lập giá trị mặc định cho rạp mới
        theater.setStatus(Theater_Status.Active); // Mặc định là Active
        theater.setRoomQuantity(0); // Số phòng ban đầu là 0

        Theater savedTheater = theaterRepository.save(theater);
        return toDTO(savedTheater);
    }

    // Phương thức private để lưu ảnh và trả về tên file
    private String saveImage(MultipartFile imageFile) throws IOException {
        // Bây giờ Paths.get() sẽ hoạt động chính xác với biến UPLOAD_DIR kiểu String
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Tạo tên file duy nhất để tránh trùng lặp
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        return fileName;
    }

    public TheaterDTO toDTO(Theater entity) {
        TheaterDTO dto = new TheaterDTO();
        dto.setTheaterID(entity.getTheaterID());
        dto.setTheaterName(entity.getTheaterName());
        dto.setAddress(entity.getAddress());
        dto.setImage(entity.getImage());
        dto.setRoomQuantity(entity.getRoomQuantity());
        dto.setServiceRate(entity.getServiceRate());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public Theater toEntity(TheaterDTO dto) {
        Theater entity = new Theater();
        entity.setTheaterID(dto.getTheaterID());
        entity.setTheaterName(dto.getTheaterName());
        entity.setAddress(dto.getAddress());
        entity.setImage(dto.getImage());
        entity.setRoomQuantity(dto.getRoomQuantity());
        entity.setServiceRate(dto.getServiceRate());
        entity.setStatus(dto.getStatus());
        if (dto.getRooms() != null) {
            entity.setRooms(dto.getRooms().stream().map(roomDTO -> roomService.toEntity(roomDTO)).collect(Collectors.toList()));
        }
        return entity;
    }
}
