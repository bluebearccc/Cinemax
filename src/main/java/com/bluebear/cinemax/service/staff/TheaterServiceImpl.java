package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.TheaterDTO;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheaterServiceImpl implements TheaterService {

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
}