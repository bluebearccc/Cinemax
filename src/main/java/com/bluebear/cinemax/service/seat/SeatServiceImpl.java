package com.bluebear.cinemax.service.seat;

import com.bluebear.cinemax.dto.SeatUpdateRequest;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.email.EmailService;
import com.bluebear.cinemax.service.invoice.InvoiceService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bluebear.cinemax.dto.SeatDTO;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatServiceImpl implements SeatService {
    @Autowired
    private DetailSeatRepository detailSeatRepository;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;

    public SeatDTO createSeat(SeatDTO dto) {
        Seat seat = toEntity(dto);
        return toDTO(seatRepository.save(seat));
    }

    public SeatDTO updateSeat(Integer seatID, SeatDTO dto) {
        Optional<Seat> optionalSeat = seatRepository.findById(seatID);
        Optional<Room> optionalRoom = roomRepository.findById(dto.getRoomID());
        if (optionalSeat.isEmpty()) return null;

        Seat seat = optionalSeat.get();
        seat.setSeatType(dto.getSeatType());
        seat.setPosition(dto.getPosition());
        seat.setIsVIP(dto.getIsVIP());
        seat.setUnitPrice(dto.getUnitPrice());
        seat.setStatus(dto.getStatus());
        if (!optionalRoom.isEmpty()) {seat.setRoom(optionalRoom.get());}

        return toDTO(seatRepository.save(seat));
    }

    public void deleteSeat(Integer seatID) {
        seatRepository.deleteById(seatID);
    }



    public Page<SeatDTO> getAllSeats() {
        return seatRepository.findByStatus(Seat_Status.Active, Pageable.unpaged())
                .map(this::toDTO);
    }

    public Page<SeatDTO> getSeatsByRoomId(Integer roomID) {
        return seatRepository.findByRoom_RoomIDAndStatus(roomID, Seat_Status.Active, Pageable.unpaged())
                .map(this::toDTO);
    }

    public SeatDTO toDTO(Seat entity) {
        SeatDTO dto = new SeatDTO();
        dto.setSeatID(entity.getSeatID());
        dto.setRoomID(entity.getRoom().getRoomID());
        dto.setSeatType(entity.getSeatType());
        dto.setPosition(entity.getPosition());
        dto.setIsVIP(entity.getIsVIP());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setStatus(entity.getStatus());
        dto.setName(entity.getName());
        return dto;
    }

    public Seat toEntity(SeatDTO dto) {
        Seat seat = new Seat();
        seat.setSeatID(dto.getSeatID());
        seat.setSeatType(dto.getSeatType());
        seat.setPosition(dto.getPosition());
        seat.setIsVIP(dto.getIsVIP());
        seat.setUnitPrice(dto.getUnitPrice());
        seat.setStatus(dto.getStatus());
        seat.setName(dto.getName() == null ? "" : dto.getName());
        Optional<Room> room = roomRepository.findById(dto.getRoomID());
        room.ifPresent(seat::setRoom);

        return seat;
    }

    @Override
    public List<SeatDTO> findAllByRoomId(Integer id) {
        List<Seat> seats = seatRepository.findByRoom_RoomID(id);
        List<SeatDTO> seatDTOS = new ArrayList<>();
        for (Seat seat : seats) {
            seatDTOS.add(toDTO(seat));
        }
        return seatDTOS;
    }

    public List<SeatDTO> getSeatsByIds(List<Integer> selectedSeatIds) {
        return seatRepository.findAllById(selectedSeatIds)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateSeatsVipAndPrice(SeatUpdateRequest request) throws Exception {
        if (request.getRoomId() == null) {
            throw new Exception("Room ID is required.");
        }
        if (request.getVipPrice() != null && request.getNonVipPrice() != null) {
            if (request.getVipPrice() < request.getNonVipPrice() + 40000) {
                throw new Exception("Price for VIP seats cannot be less than the price for non-VIP seats plus 40,000. Please check and try again.");
            }
        }
        List<Seat> seatsInRoom = seatRepository.findByRoom_RoomID(request.getRoomId());
        List<Integer> vipSeatIds = request.getVipSeatIds() != null ? request.getVipSeatIds() : new ArrayList<>();

        Map<Integer, String> newPositions = request.getPositions();

        if (newPositions != null && !newPositions.isEmpty()) {
            long distinctPositions = newPositions.values().stream().distinct().count();
            if (distinctPositions < newPositions.size()) {
                throw new Exception("Duplicate position values found in the request. Please check and try again.");
            }

            for (Seat seat : seatsInRoom) {
                if (newPositions.containsKey(seat.getSeatID())) {
                    String newPositionValue = newPositions.get(seat.getSeatID());
                    seat.setPosition(newPositionValue);
                }
            }
        }

        for (Seat seat : seatsInRoom) {
            if (vipSeatIds.contains(seat.getSeatID())) {
                seat.setIsVIP(true);
                seat.setUnitPrice(request.getVipPrice());
            } else {
                seat.setIsVIP(false);
                seat.setUnitPrice(request.getNonVipPrice());
            }
        }

        seatRepository.saveAll(seatsInRoom);
    }
    @Override
    @Transactional
    public void resetSeatNamesInRoom(Integer roomId) {
        List<Seat> allSeats = seatRepository.findByRoom_RoomIDOrderByPositionAsc(roomId);

        Map<Character, List<Seat>> seatsByRow = new LinkedHashMap<>();
        for (Seat seat : allSeats) {
            char rowChar = seat.getPosition().charAt(0);
            if (seatsByRow.containsKey(rowChar)) {
                seatsByRow.get(rowChar).add(seat);
            } else {
                List<Seat> newList = new ArrayList<>();
                newList.add(seat);
                seatsByRow.put(rowChar, newList);
            }
        }

        for (List<Seat> rowSeats : seatsByRow.values()) {
            for (int i = 0; i < rowSeats.size(); i++) {
                Seat currentSeat = rowSeats.get(i);
                char rowChar = currentSeat.getPosition().charAt(0);

                String newName = rowChar + String.valueOf(i + 1);
                currentSeat.setName(newName);
            }
        }

        seatRepository.saveAll(allSeats);
    }
    @Override
    @Transactional
    public List<SeatDTO> deleteSeatById(Integer seatId) throws Exception {

        long bookingCount = detailSeatRepository.countBySeat_SeatID(seatId);

        if (bookingCount > 0) {
            throw new Exception("Cannot delete this seat because it is part of existing bookings. Consider setting its status to 'Inactive' instead.");
        }

        Seat seatToDelete = seatRepository.findById(seatId)
                .orElseThrow(() -> new Exception("Seat not found with id: " + seatId));
        Integer roomId = seatToDelete.getRoom().getRoomID();

        seatRepository.delete(seatToDelete);

        this.resetSeatNamesInRoom(roomId);

        List<Seat> updatedSeats = seatRepository.findByRoom_RoomIDOrderByPositionAsc(roomId);
        return updatedSeats.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional
    public void updateSeatsInRoom(SeatUpdateRequest request) {
        Integer roomId = request.getRoomId();
        List<Integer> vipSeatIds = request.getVipSeatIds() != null ? request.getVipSeatIds() : new ArrayList<>();
        Map<Integer, Seat_Status> newStatuses = request.getSeatStatuses();

        List<Seat> seatsInRoom = seatRepository.findByRoom_RoomID(roomId);
        if (seatsInRoom == null || seatsInRoom.isEmpty()) {
            return;
        }

        Map<Integer, String> notificationsToSend = new HashMap<>();

        for (Seat seat : seatsInRoom) {
            // --- LẤY THÔNG TIN CŨ VÀ MỚI ---
            Seat_Status oldStatus = seat.getStatus();
            double oldPrice = seat.getUnitPrice();
            boolean isNowVip = vipSeatIds.contains(seat.getSeatID());
            Seat_Status newStatus = (newStatuses != null) ? newStatuses.get(seat.getSeatID()) : oldStatus;
            Double newPrice = isNowVip ? request.getVipPrice() : request.getNonVipPrice();
            String notificationReason = null;
            if (oldStatus == Seat_Status.Active && newStatus != Seat_Status.Active) {
                notificationReason = String.format("your selected seat **%s** has become unavailable due to technical reasons.", seat.getPosition());
            }
            else if (newPrice != null && Math.abs(newPrice - oldPrice) > 0.01) {
                notificationReason = String.format(
                        "the price for your seat **%s** has been updated from **%,.0f VND** to **%,.0f VND**.",
                        seat.getPosition(), oldPrice, newPrice
                );
            }

            if (notificationReason != null) {
                List<DetailSeat> futureBookings = detailSeatRepository.findFutureBookingsBySeatID(seat.getSeatID(), LocalDateTime.now());
                for (DetailSeat affectedBooking : futureBookings) {
                    notificationsToSend.put(affectedBooking.getInvoice().getInvoiceID(), notificationReason);
                }
            }

            // --- CẬP NHẬT THUỘC TÍNH CỦA GHẾ ---
            seat.setIsVIP(isNowVip);
            if (newPrice != null) {
                seat.setUnitPrice(newPrice);
            }
            if (newStatus != null) {
                seat.setStatus(newStatus);
            }
        }

        // --- GỬI EMAIL THÔNG BÁO ---
        if (!notificationsToSend.isEmpty()) {
            for (Map.Entry<Integer, String> entry : notificationsToSend.entrySet()) {
                Integer invoiceId = entry.getKey();
                String reason = entry.getValue();

                Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                if (invoice == null) continue;

                String recipientEmail = null, recipientName = "Valued Customer";
                if (invoice.getCustomer() != null && invoice.getCustomer().getAccount() != null) {
                    recipientEmail = invoice.getCustomer().getAccount().getEmail();
                    recipientName = invoice.getCustomer().getFullName();
                } else if (invoice.getGuestEmail() != null) {
                    recipientEmail = invoice.getGuestEmail();
                    recipientName = invoice.getGuestName() != null ? invoice.getGuestName() : recipientName;
                }

                if (recipientEmail != null) {
                    String movieName = invoice.getDetailSeats().get(0).getSchedule().getMovie().getMovieName();
                    String subject = "Important Update Regarding Your Booking for '" + movieName + "'";

                    // Nội dung email chung cho mọi thay đổi
                    String body = String.format(
                            "Dear %s,\n\n" +
                                    "We are writing to inform you about an update regarding your booking: %s\n\n" +
                                    "Please review this change. If you have any questions, please do not hesitate to contact our support team.\n\n" +
                                    "Sincerely,\n" +
                                    "The Cinemax Team",
                            recipientName,
                            reason
                    );
                    emailService.sendNotifyScheduleEmail(recipientEmail, subject, body);
                }
            }
        }

        seatRepository.saveAll(seatsInRoom);
    }
    public List<SeatDTO> getSeatsWithStatus(Integer roomId, Integer scheduleId) {
        List<Seat> seats = seatRepository.findByRoomRoomID(roomId);
        List<SeatDTO> seatDTOs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Seat seat : seats) {
            List<DetailSeat> relatedSeats = detailSeatRepository.findBySeatSeatIDAndScheduleScheduleIDAndStatusIn(
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
    public SeatDTO getSeatById(Integer seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seatId));
        return toDTO(seat);
    }
    @Override
    public List<Integer> getUnpaidSeatIdsForSchedule(Integer scheduleId) {
        return detailSeatRepository.findSeatIdsByScheduleIdAndStatus(scheduleId, DetailSeat_Status.Unpaid);
    }
}

