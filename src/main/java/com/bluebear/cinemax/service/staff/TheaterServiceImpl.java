package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
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

    private Theater convertToEntity(TheaterDTO theaterDTO) {
        if (theaterDTO == null) {
            return null;
        }
        Theater theater = new Theater();
        theater.setTheaterID(theaterDTO.getTheaterID());
        theater.setTheaterName(theaterDTO.getTheaterName());
        theater.setAddress(theaterDTO.getAddress());
        theater.setImage(theaterDTO.getImage());
        theater.setRoomQuantity(theaterDTO.getRoomQuantity());
        theater.setStatus(theaterDTO.getStatus());
        return theater;
    }

    private TheaterDTO convertToDTO(Theater theater) {
        if (theater == null) {
            return null;
        }
        return TheaterDTO.builder()
                .theaterID(theater.getTheaterID())
                .theaterName(theater.getTheaterName())
                .address(theater.getAddress())
                .image(theater.getImage())
                .roomQuantity(theater.getRoomQuantity())
                .status(theater.getStatus())
                .build();
    }


    @Override
    public TheaterDTO getTheaterById(Integer id) {
        Optional<Theater> theaterOptional = theaterRepository.findById(id);
        return theaterOptional.map(this::convertToDTO).orElse(null);
    }

    @Override
    public List<TheaterDTO> findAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(this::convertToDTO)
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
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TheaterDTO> findAllTheatersByName(String keyword) {
        List<Theater> theaters = theaterRepository.findByTheaterNameContainingIgnoreCase(keyword);
        return theaters.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public TheaterDTO saveTheater(TheaterDTO theaterDTO) {
        Theater theaterToSave = convertToEntity(theaterDTO);
        Theater savedTheater = theaterRepository.save(theaterToSave);
        return convertToDTO(savedTheater);
    }

    public TheaterDTO updateTheater(Integer id, TheaterDTO theaterDTO) {
        Optional<Theater> existingTheaterOptional = theaterRepository.findById(id);
        if (existingTheaterOptional.isPresent()) {
            Theater existingTheater = existingTheaterOptional.get();

            existingTheater.setTheaterName(theaterDTO.getTheaterName());
            existingTheater.setAddress(theaterDTO.getAddress());
            existingTheater.setImage(theaterDTO.getImage());
            existingTheater.setRoomQuantity(theaterDTO.getRoomQuantity());
            existingTheater.setStatus(theaterDTO.getStatus());

            Theater updatedTheater = theaterRepository.save(existingTheater);
            return convertToDTO(updatedTheater);
        }
        return null;
    }


    public boolean deleteTheater(Integer id) {
        try {
            theaterRepository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException e) {
            System.err.println("Cannot delete Theater with ID " + id + " due to data integrity violation: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("An error occurred while deleting Theater with ID " + id + ": " + e.getMessage());
            return false;
        }
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

        Theater theater = convertToEntity(theaterDTO);

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
        return convertToDTO(savedTheater);
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
}