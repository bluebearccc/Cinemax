package com.bluebear.cinemax.service.theater;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.service.room.RoomService;
import jakarta.transaction.Transactional;
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
import java.nio.file.StandardCopyOption;
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
        if (!theaterDTO.getTheaterName().trim().toLowerCase().startsWith("cgv")) {
            throw new Exception("Theater 's name must start with 'CGV'.");
        }

        // Validation 2: Tên rạp không được trùng
        if (theaterRepository.existsByTheaterNameIgnoreCase(theaterDTO.getTheaterName().trim())) {
            throw new Exception("This theater name is already taken. Please choose another name.");
        }

        // Validation 3: Địa chỉ không được trùng
        if (theaterRepository.existsByAddressIgnoreCase(theaterDTO.getAddress().trim())) {
            throw new Exception("Theater address is already taken. Please choose another address.");
        }

        Theater theater = toEntity(theaterDTO);

        // Xử lý upload ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            theater.setImage("/uploads/theater_images/" + fileName);
        }

        // Thiết lập giá trị mặc định cho rạp mới
        theater.setStatus(Theater_Status.Active); // Mặc định là Active
        theater.setRoomQuantity(0); // Số phòng ban đầu là 0

        Theater savedTheater = theaterRepository.save(theater);
        return toDTO(savedTheater);
    }


    public String saveImage(MultipartFile img) throws IOException {
        String uploadDir = "uploads/theater_images";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = img.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        if (Files.exists(filePath)) {
            int counter = 1;
            String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'));
            String extension = filename.substring(filename.lastIndexOf('.'));
            while (Files.exists(filePath)) {
                filename = nameWithoutExtension + "(" + counter + ")" + extension;
                filePath = uploadPath.resolve(filename);
                counter++;
            }
        }
        Files.copy(img.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
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
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
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
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        if (dto.getRooms() != null) {
            entity.setRooms(dto.getRooms().stream().map(roomDTO -> roomService.toEntity(roomDTO)).collect(Collectors.toList()));
        }
        return entity;
    }
    @Override
    public Page<TheaterDTO> findByKeywordAndStatusPaginated(String keyword, String status, Pageable pageable) {
        try {
            Theater_Status statusEnum = Theater_Status.valueOf(status);
            return theaterRepository.findByTheaterNameContainingIgnoreCaseAndStatus(keyword, statusEnum, pageable).map(this::toDTO);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
    @Override
    public Page<TheaterDTO> findByKeywordPaginated(String keyword, Pageable pageable) {
        return theaterRepository.findByTheaterNameContainingIgnoreCase(keyword, pageable).map(this::toDTO);
    }
    @Override
    public Page<TheaterDTO> findByStatusPaginated(String status, Pageable pageable) {
        try {
            Theater_Status statusEnum = Theater_Status.valueOf(status);
            return theaterRepository.findByStatus(statusEnum, pageable).map(this::toDTO);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
    @Override
    public Page<TheaterDTO> findAllPaginated(Pageable pageable) {
        return theaterRepository.findAll(pageable).map(this::toDTO);
    }
    @Override
    @Transactional
    public void updateTheater(TheaterDTO theaterDTO) throws IOException {
        Theater theaterToUpdate = theaterRepository.findById(theaterDTO.getTheaterID())
                .orElseThrow(() -> new IllegalArgumentException("Theater not found with ID: " + theaterDTO.getTheaterID()));

        if (theaterRepository.existsByTheaterNameIgnoreCaseAndTheaterIDNot(theaterDTO.getTheaterName(), theaterDTO.getTheaterID())) {
            throw new IllegalArgumentException("Another theater with the name '" + theaterDTO.getTheaterName() + "' already exists.");
        }

        theaterToUpdate.setTheaterName(theaterDTO.getTheaterName());
        theaterToUpdate.setAddress(theaterDTO.getAddress());
        theaterToUpdate.setRoomQuantity(theaterDTO.getRoomQuantity());
        theaterToUpdate.setStatus(theaterDTO.getStatus());
        theaterToUpdate.setLatitude(theaterDTO.getLatitude());
        theaterToUpdate.setLongitude(theaterDTO.getLongitude());
        MultipartFile newImageFile = theaterDTO.getNewImage();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            String newFileName = saveImage(newImageFile);
            theaterToUpdate.setImage("/uploads/theaters_images/" + newFileName);
        }

    }

    @Override
    public TheaterDTO getTheaterByName(String name) {
        return toDTO(theaterRepository.findByTheaterNameContainingIgnoreCase(name).getFirst());
    }

}
