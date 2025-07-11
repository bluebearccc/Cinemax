package com.bluebear.cinemax.service.servicefeedback;

import com.bluebear.cinemax.dto.ServiceFeedbackDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.ServiceFeedback;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.ServiceFeedbackRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceFeedbackServiceImpl implements ServiceFeedbackService {

    private final ServiceFeedbackRepository feedbackRepository;
    private final CustomerRepository customerRepository;
    private final TheaterRepository theaterRepository;

    @Override
    public ServiceFeedbackDTO toDTO(ServiceFeedback entity) {
        if (entity == null) return null;

        return ServiceFeedbackDTO.builder()
                .id(entity.getId())
                .customerId(entity.getCustomer().getId())
                .createdDate(entity.getCreatedDate())
                .content(entity.getContent())
                .theaterId(entity.getTheater().getTheaterID())
                .serviceRate(entity.getServiceRate())
                .status(entity.getStatus())
                .build();
    }

    @Override
    public ServiceFeedback toEntity(ServiceFeedbackDTO dto) {
        if (dto == null) return null;

        Optional<Customer> customer = customerRepository.findById(dto.getCustomerId());
        Optional<Theater> theater = theaterRepository.findById(dto.getTheaterId());

        if (customer.isEmpty() || theater.isEmpty()) {
            throw new RuntimeException("Customer or Theater not found");
        }

        return ServiceFeedback.builder()
                .id(dto.getId())
                .customer(customer.get())
                .createdDate(dto.getCreatedDate()) // có thể để null để auto tạo
                .content(dto.getContent())
                .theater(theater.get())
                .serviceRate(dto.getServiceRate())
                .status(dto.getStatus())
                .build();
    }

    @Override
    public ServiceFeedbackDTO create(ServiceFeedbackDTO dto) {
        ServiceFeedback saved = feedbackRepository.save(toEntity(dto));
        return toDTO(saved);
    }

    @Override
    public ServiceFeedbackDTO update(Integer id, ServiceFeedbackDTO dto) {
        Optional<ServiceFeedback> existing = feedbackRepository.findById(id);
        if (existing.isEmpty()) throw new RuntimeException("Feedback not found");

        ServiceFeedback entity = toEntity(dto);
        entity.setId(id); // giữ nguyên ID
        ServiceFeedback updated = feedbackRepository.save(entity);
        return toDTO(updated);
    }

    @Override
    public boolean delete(Integer id) {
        if (!feedbackRepository.existsById(id)) return false;
        feedbackRepository.deleteById(id);
        return true;
    }

    @Override
    public ServiceFeedbackDTO getById(Integer id) {
        return feedbackRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public List<ServiceFeedbackDTO> getAll() {
        return feedbackRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countByTheaterId(Integer theaterId) {
        return (int) feedbackRepository.countByTheater_theaterID(theaterId);
    }
}

