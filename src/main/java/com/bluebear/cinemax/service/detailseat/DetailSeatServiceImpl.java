package com.bluebear.cinemax.service.detailseat;

import com.bluebear.cinemax.dto.DetailSeatDTO;
import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Schedule;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.ScheduleRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DetailSeatServiceImpl implements DetailSeatService {
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    
    public DetailSeatDTO createDetailSeat(DetailSeatDTO dto) {
        DetailSeat detailSeat = toEntity(dto);
        return toDTO(detailSeatRepository.save(detailSeat));
    }

    
    public DetailSeatDTO getDetailSeatById(Integer id) {
        return detailSeatRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    
    public List<DetailSeatDTO> getAllDetailSeats() {
        return detailSeatRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public boolean hasCustomerWatched(int customerId, int movieId) {
        return detailSeatRepository.hasCustomerWatchedMovie(customerId, movieId, InvoiceStatus.Booked);
    }
    
    public DetailSeatDTO updateDetailSeat(Integer id, DetailSeatDTO dto) {
        Optional<DetailSeat> optional = detailSeatRepository.findById(id);
        if (optional.isPresent()) {
            DetailSeat updated = toEntity(dto);
            updated.setId(id); // Giữ lại ID cũ
            return toDTO(detailSeatRepository.save(updated));
        }
        return null;
    }

    public void deleteDetailSeat(Integer id) {
        detailSeatRepository.deleteById(id);
    }

    public Page<DetailSeatDTO> getDetailSeatsByScheduleId(Integer scheduleId, Pageable pageable) {
        return detailSeatRepository.findBySchedule_ScheduleID(scheduleId, pageable)
                .map(this::toDTO);
    }

    public int countDetailSeatByScheduleId(int scheduleId) {
        return (int) detailSeatRepository.countBySchedule_ScheduleID(scheduleId);
    }

    public DetailSeatDTO toDTO(DetailSeat entity) {
        return DetailSeatDTO.builder()
                .id(entity.getId())
                .invoiceID(entity.getInvoice().getInvoiceID())
                .seatID(entity.getSeat().getSeatID())
                .scheduleID(entity.getSchedule().getScheduleID())
                .build();
    }

    public DetailSeat toEntity(DetailSeatDTO dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceID()).orElseThrow();
        Seat seat = seatRepository.findById(dto.getSeatID()).orElseThrow();
        Schedule schedule = scheduleRepository.findById(dto.getScheduleID()).orElseThrow();

        return DetailSeat.builder()
                .id(dto.getId())
                .invoice(invoice)
                .seat(seat)
                .schedule(schedule)
                .build();
    }

    @Override
    public List<Integer> findBookedSeatIdsByScheduleId(Integer scheduleId) {
        return detailSeatRepository.findBookedSeatIdsByScheduleId(scheduleId);
    }
}

