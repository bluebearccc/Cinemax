package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.enumtype.TheaterStock_Status;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.repository.TheaterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheaterStockServiceImpl implements TheaterStockService {

    @Autowired
    private TheaterStockRepository theaterStockRepository;

    @Autowired
    private TheaterRepository theaterRepository; // Injected to handle the Theater relationship


    private TheaterStock convertToEntity(TheaterStockDTO theaterStockDTO) {
        if (theaterStockDTO == null) {
            return null;
        }

        TheaterStock.TheaterStockBuilder builder = TheaterStock.builder()
                .stockID(theaterStockDTO.getTheaterStockId()) // Gán ID nếu đây là trường hợp cập nhật
                .itemName(theaterStockDTO.getFoodName())      // Ánh xạ foodName (DTO) -> itemName (Entity)
                .image(theaterStockDTO.getImage())
                .quantity(theaterStockDTO.getQuantity())
                .price(theaterStockDTO.getUnitPrice());     // Ánh xạ unitPrice (DTO) -> price (Entity)

        if (theaterStockDTO.getStatus() != null && !theaterStockDTO.getStatus().isEmpty()) {
            try {
                // Chuyển sang chữ hoa để khớp với tên của Enum
                builder.status(TheaterStock_Status.valueOf(theaterStockDTO.getStatus()));
            } catch (IllegalArgumentException e) {
                System.err.println("Giá trị status không hợp lệ: " + theaterStockDTO.getStatus());
            }
        }

        // --- SỬA LỖI LOGIC QUAN TRỌNG ---
        // Lấy Theater từ DB dựa trên theaterID trong TheaterDTO lồng nhau
        if (theaterStockDTO.getTheater() != null && theaterStockDTO.getTheater().getTheaterID() != null) {
            theaterRepository.findById(theaterStockDTO.getTheater().getTheaterID())
                    .ifPresent(builder::theater); // Gán theater tìm thấy cho builder
        }

        return builder.build();
    }

    /**
     * Chuyển đổi từ TheaterStock (Entity) sang TheaterStockDTO.
     *
     * @param theaterStock Đối tượng Entity đầu vào.
     * @return Đối tượng DTO.
     */
    private TheaterStockDTO convertToDTO(TheaterStock theaterStock) {
        if (theaterStock == null) {
            return null;
        }

        return TheaterStockDTO.builder()
                .theaterStockId(theaterStock.getStockID())
                .theater(convertToTheaterDTO(theaterStock.getTheater())) // Gọi hàm helper để chuyển đổi Theater
                .foodName(theaterStock.getItemName())
                .image(theaterStock.getImage())
                .quantity(theaterStock.getQuantity())
                .unitPrice(theaterStock.getPrice())
                .status(theaterStock.getStatus().name()) // Chuyển Enum thành String an toàn
                .build();
    }


    private TheaterDTO convertToTheaterDTO(Theater theater) {
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
                // Cố ý không set 'rooms' và 'theaterStockS' để tránh lỗi lặp vô hạn
                .build();
    }


    @Override
    public List<TheaterStockDTO> findByTheaterId(Integer theaterId) {
        List<TheaterStock> theaterStocks = theaterStockRepository.findByTheater_TheaterId(theaterId);

        return theaterStocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TheaterStockDTO getTheaterStockById(Integer id) {
        Optional<TheaterStock> theaterStockOptional = theaterStockRepository.findById(id);
        return theaterStockOptional.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional // Ensures database operations are atomic
    public void saveTheaterStock(TheaterStockDTO theaterStockDTO) {
        // This method now handles both creation and update based on if DTO has an ID
        // If DTO has an ID, it will update; otherwise, it will create.
        TheaterStock theaterStockToSave = convertToEntity(theaterStockDTO);
        theaterStockRepository.save(theaterStockToSave);
    }

    // You had an extra findById, consolidating it into getTheaterStockById
    // The previous findById(Integer id) in your interface will now just call getTheaterStockById(id)
    // to maintain compatibility with your existing interface.
    public TheaterStockDTO findById(Integer id) {
        return getTheaterStockById(id);
    }


    @Override
    public List<TheaterStockDTO> findAllTheaterStock() {
        List<TheaterStock> theaterStocks = theaterStockRepository.findAll();
        return theaterStocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TheaterStockDTO> findByItemName(String itemName, Integer theaterId) {
        // Assuming a repository method that can filter by both item name (case-insensitive) and theater ID
        // You might need to add 'findByItemNameContainingIgnoreCaseAndTheater_TheaterId' to your TheaterStockRepository
        List<TheaterStock> theaterStocks = theaterStockRepository.findByItemNameContainingIgnoreCase(itemName, theaterId);
        return theaterStocks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDeleted(Integer id) {
        try {
            theaterStockRepository.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Log the exception for debugging
            System.err.println("Cannot delete TheaterStock with ID " + id + " due to data integrity violation: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Catch other potential exceptions during deletion
            System.err.println("An error occurred while deleting TheaterStock with ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean itemExistsInTheater(String itemName, Integer theaterId) {
        return theaterStockRepository.existsByItemNameIgnoreCaseAndTheater_TheaterID(itemName, theaterId);
    }


    @Override
    public Page<TheaterStockDTO> getAllTheaterStock(Pageable pageable) {
        Page<TheaterStock> theaterStocksPage = theaterStockRepository.findAll(pageable);
        return theaterStocksPage.map(this::convertToDTO);
    }
    @Override
    public Page<TheaterStockDTO> findByTheaterIdAndItemName(Integer theaterId, String itemName, Pageable pageable) {
        // Gọi phương thức repository tương ứng
        Page<TheaterStock> theaterStockPage = theaterStockRepository.findByTheater_TheaterIDAndItemNameContainingIgnoreCase(
                theaterId, itemName, pageable);

        // Chuyển đổi Page<Entity> sang Page<DTO> và trả về
        return theaterStockPage.map(this::convertToDTO);
    }

    @Override
    public Optional<TheaterStockDTO> findFirstByItemName(String itemName) {
        Optional<TheaterStock> stockOptionalEntity = theaterStockRepository.findFirstByItemNameIgnoreCase(itemName);
        return stockOptionalEntity.map(this::convertToDTO);
    }

    @Override
    public List<TheaterStockDTO> findAllByItemName(String itemName) {
        return theaterStockRepository.findByItemNameIgnoreCase(itemName)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional // Rất quan trọng để đảm bảo tất cả các update thành công hoặc không có gì cả
    @Override
    public void updateItemAcrossAllTheaters(TheaterStockDTO formData) throws IOException {
        TheaterStock originalItem = theaterStockRepository.findById(formData.getTheaterStockId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found with ID: " + formData.getTheaterStockId()));

        String originalItemName = originalItem.getItemName();

        if (!originalItemName.equalsIgnoreCase(formData.getFoodName())) {
            if (theaterStockRepository.findFirstByItemNameIgnoreCase(formData.getFoodName()).isPresent()) {
                throw new IllegalArgumentException("Item name '" + formData.getFoodName() + "' already exists.");
            }
        }

        List<TheaterStock> itemsToUpdate = theaterStockRepository.findByItemNameIgnoreCase(originalItemName);
        String imagePathToSet = originalItem.getImage(); // Mặc định giữ ảnh cũ
        MultipartFile newImageFile = formData.getNewImageFile();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            String newFileName = saveImage(newImageFile);
            imagePathToSet = "/uploads/images/" + newFileName;
        }
        for (TheaterStock item : itemsToUpdate) {
            item.setItemName(formData.getFoodName());
            item.setPrice(formData.getUnitPrice());
            item.setImage(imagePathToSet);
        }
        originalItem.setQuantity(formData.getQuantity());
        originalItem.setStatus(TheaterStock_Status.valueOf(formData.getStatus()));
    }
    public String saveImage(MultipartFile img) throws IOException {
        String uploadDir = "uploads/images";
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

        return filename; // Chỉ trả về tên file
    }
    @Override
    public Page<TheaterStockDTO> findByTheaterId(Integer theaterId, Pageable pageable) {
        return theaterStockRepository.findByTheater_TheaterID(theaterId, pageable).map(this::convertToDTO);
    }

    @Override
    public Page<TheaterStockDTO> findByItemName(String itemName, Pageable pageable) {
        return theaterStockRepository.findByItemNameContainingIgnoreCase(itemName, pageable).map(this::convertToDTO);
    }



}