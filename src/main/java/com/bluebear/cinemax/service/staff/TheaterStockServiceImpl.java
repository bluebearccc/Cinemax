package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterStockDTO;
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.entity.TheaterStock;
import com.bluebear.cinemax.repository.TheaterRepository;
import com.bluebear.cinemax.repository.TheaterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        TheaterStock theaterStock = new TheaterStock();
        theaterStock.setStockID(theaterStockDTO.getId()); // Assuming DTO's 'id' maps to entity's 'stockID'
        theaterStock.setItemName(theaterStockDTO.getItemName());
        theaterStock.setImage(theaterStockDTO.getImage());
        theaterStock.setQuantity(theaterStockDTO.getQuantity());
        theaterStock.setPrice(theaterStockDTO.getPrice());
        theaterStock.setStatus(theaterStockDTO.getStatus());

        // Set the associated Theater entity
        if (theaterStockDTO.getTheaterId() != null) {
            theaterRepository.findById(theaterStockDTO.getTheaterId())
                    .ifPresent(theaterStock::setTheater);
        } else {
            // If theaterId is null in DTO, ensure no Theater is associated
            theaterStock.setTheater(null);
        }

        return theaterStock;
    }

    /**
     * Converts a TheaterStock entity to a TheaterStockDTO.
     * Extracts theaterId from the associated Theater entity.
     *
     * @param theaterStock The entity to convert.
     * @return The converted TheaterStockDTO.
     */
    private TheaterStockDTO convertToDTO(TheaterStock theaterStock) {
        if (theaterStock == null) {
            return null;
        }
        return TheaterStockDTO.builder()
                .id(theaterStock.getStockID())
                .theaterId(theaterStock.getTheater() != null ? theaterStock.getTheater().getTheaterID() : null)
                .itemName(theaterStock.getItemName())
                .image(theaterStock.getImage())
                .quantity(theaterStock.getQuantity())
                .price(theaterStock.getPrice())
                .status(theaterStock.getStatus())
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
    @Override
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

    // You might also want to explicitly add an update method if `saveTheaterStock` isn't
    // strictly handling all update scenarios as desired, though `save` can often perform upsert.
    // However, given your interface, `saveTheaterStock` is expected to handle updates if ID is present.
    // If you add an explicit `updateTheaterStock` to the interface later, uncomment this:
    /*
    @Transactional
    public TheaterStockDTO updateTheaterStock(Integer id, TheaterStockDTO theaterStockDTO) {
        Optional<TheaterStock> existingTheaterStockOptional = theaterStockRepository.findById(id);
        if (existingTheaterStockOptional.isPresent()) {
            TheaterStock existingTheaterStock = existingTheaterStockOptional.get();

            existingTheaterStock.setItemName(theaterStockDTO.getItemName());
            existingTheaterStock.setImage(theaterStockDTO.getImage());
            existingTheaterStock.setQuantity(theaterStockDTO.getQuantity());
            existingTheaterStock.setPrice(theaterStockDTO.getPrice());
            existingTheaterStock.setStatus(theaterStockDTO.getStatus());

            if (theaterStockDTO.getTheaterId() != null) {
                theaterRepository.findById(theaterStockDTO.getTheaterId())
                        .ifPresent(existingTheaterStock::setTheater);
            } else {
                existingTheaterStock.setTheater(null);
            }

            TheaterStock updatedTheaterStock = theaterStockRepository.save(existingTheaterStock);
            return convertToDTO(updatedTheaterStock);
        }
        return null; // TheaterStock not found
    }
    */
}