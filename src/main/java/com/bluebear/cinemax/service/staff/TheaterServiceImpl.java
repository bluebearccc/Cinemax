package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.enumtype.Theater_Status;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
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
        theater.setLatitude(theaterDTO.getLatitude());
        theater.setLongtitude(theaterDTO.getLongtitude());
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
                .longtitude(theater.getLongtitude())
                .latitude(theater.getLatitude())
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
        if (theaterRepository.existsByTheaterNameIgnoreCase(theaterDTO.getTheaterName())) {
            throw new IllegalArgumentException("Theater with name '" + theaterDTO.getTheaterName() + "' already exists.");
        }

         if (theaterRepository.existsByAddressIgnoreCase(theaterDTO.getAddress())) {
             throw new IllegalArgumentException("Another theater with this address already exists.");
         }

        Theater theaterToSave = convertToEntity(theaterDTO);
        Theater savedTheater = theaterRepository.save(theaterToSave);
        return convertToDTO(savedTheater);
    }

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
        theaterToUpdate.setLongtitude(theaterDTO.getLongtitude());
        MultipartFile newImageFile = theaterDTO.getNewImage();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            String newFileName = saveImage(newImageFile);
            theaterToUpdate.setImage("/uploads/theaters_images/" + newFileName);
        }

    }



    @Override
    public TheaterDTO addTheater(TheaterDTO theaterDTO, MultipartFile imageFile) throws Exception {
        if (!theaterDTO.getTheaterName().trim().toLowerCase().startsWith("cgv")) {
            throw new Exception("Theater name must start with 'CGV'.");
        }

        if (theaterRepository.existsByTheaterNameIgnoreCase(theaterDTO.getTheaterName().trim())) {
            throw new Exception("Theater name already existed please select another name.");
        }

        if (theaterRepository.existsByAddressIgnoreCase(theaterDTO.getAddress().trim())) {
            throw new Exception("This address is already existed please select another address.");
        }

        Theater theater = convertToEntity(theaterDTO);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveImage(imageFile);
            theater.setImage("/uploads/theaters_images/" + fileName);
        }

        theater.setStatus(theater.getStatus());
        theater.setRoomQuantity(0); // Số phòng ban đầu là 0

        Theater savedTheater = theaterRepository.save(theater);
        return convertToDTO(savedTheater);
    }
    public String saveImage(MultipartFile img) throws IOException {
        String uploadDir = "uploads/theaters_images";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = img.getOriginalFilename();
        filename = org.springframework.util.StringUtils.cleanPath(filename);
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
    @Transactional
    public void deleteTheater(Integer theaterId) throws Exception {
        // BƯỚC 1: KIỂM TRA SỰ TỒN TẠI CỦA CÁC PHÒNG
        long roomCount = roomRepository.countByTheater_TheaterID(theaterId);

        // Nếu số phòng lớn hơn 0, ném ra lỗi và không cho xóa
        if (roomCount > 0) {
            throw new Exception("Cannot delete this theater as it contains " + roomCount + " room(s). Please delete all rooms in this theater first.");
        }

        // BƯỚC 2: KIỂM TRA RẠP HÁT CÓ TỒN TẠI KHÔNG
        if (!theaterRepository.existsById(theaterId)) {
            throw new Exception("Theater not found with ID: " + theaterId);
        }

        // BƯỚC 3: TIẾN HÀNH XÓA NẾU HỢP LỆ
        theaterRepository.deleteById(theaterId);
    }
    @Override
    public Page<TheaterDTO> findAllPaginated(Pageable pageable) {
        return theaterRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public Page<TheaterDTO> findByKeywordPaginated(String keyword, Pageable pageable) {
        return theaterRepository.findByTheaterNameContainingIgnoreCase(keyword, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<TheaterDTO> findByStatusPaginated(String status, Pageable pageable) {
        try {
            Theater_Status statusEnum = Theater_Status.valueOf(status);
            return theaterRepository.findByStatus(statusEnum, pageable).map(this::convertToDTO);
        } catch (Exception e) {
            return Page.empty(pageable); // Trả về trang trống nếu status không hợp lệ
        }
    }

    @Override
    public Page<TheaterDTO> findByKeywordAndStatusPaginated(String keyword, String status, Pageable pageable) {
        try {
            Theater_Status statusEnum = Theater_Status.valueOf(status);
            return theaterRepository.findByTheaterNameContainingIgnoreCaseAndStatus(keyword, statusEnum, pageable).map(this::convertToDTO);
        } catch (Exception e) {
            return Page.empty(pageable);
        }
    }
}