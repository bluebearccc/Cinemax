package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatServiceImp implements SeatService  {
    @Autowired
    private DetailSeatRepository detailSeatRepo;
    @Autowired
    private SeatRepository seatRepo;
    public List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId) {
        List<Seat> seats = seatRepo.findByRoomRoomID(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Seat seat : seats) {
            List<DetailSeat> relatedSeats = detailSeatRepo.findBySeatSeatIDAndScheduleScheduleIDAndStatusIn(
                    seat.getSeatID(), scheduleId,
                    List.of(DetailSeat_Status.Unpaid, DetailSeat_Status.Booked)
            );

            boolean isBooked = false;

            for (DetailSeat ds : relatedSeats) {
                Invoice invoice = ds.getInvoice();
                if (ds.getStatus() == DetailSeat_Status.Booked) {
                    isBooked = true;
                    break;
                }

                if (ds.getStatus() == DetailSeat_Status.Unpaid &&
                        invoice != null &&
                        invoice.getBookingDate() != null &&
                        invoice.getBookingDate().isAfter(now.minusMinutes(15))) {
                    // Ghế vẫn đang được giữ < 15 phút => xem là booked
                    isBooked = true;
                    break;
                }
            }

            SeatDTO dto = toDTO(seat);
            dto.setBooked(isBooked); // true nếu đang bị giữ hoặc đã thanh toán
            seatDTOs.add(dto);
        }

        return seatDTOs;
    }
    public List<SeatDTO> toSeatDTOList(List<Seat> seats) {
        return seats.stream().map(this::toDTO).toList();
    }
    public SeatDTO toDTO(Seat seat) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(seat.getSeatID());
        dto.setRoomID(seat.getRoom().getRoomID());
        dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType() : null);
        dto.setPosition(seat.getPosition());
        dto.setIsVIP(seat.isVIP());
        dto.setUnitPrice(seat.getUnitPrice());
        dto.setStatus(seat.getStatus() != null ? seat.getStatus() : null);
        return dto;
    }
}
