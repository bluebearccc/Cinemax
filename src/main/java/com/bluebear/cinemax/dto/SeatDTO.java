package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Seat;
import com.bluebear.cinemax.enumtype.Seat_Status;
import com.bluebear.cinemax.enumtype.TypeOfSeat;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Integer seatID;
    private Integer roomID;
    private TypeOfSeat seatType;
    private String position;
    private Boolean isVIP;
    private Double unitPrice;
    private boolean isBooked;
    private Seat_Status status;

    public SeatDTO(Seat seat) {
        this.seatID = seat.getSeatId();
        this.roomID = seat.getRoom().getRoomID();
        this.seatType = mapSeatType(seat.getSeatType());
        this.position = seat.getPosition();
        this.isVIP = seat.isVIP();
        this.unitPrice = seat.getUnitPrice();
        this.status = mapSeatStatus(seat.getStatus());
        this.isBooked = false;
    }
    public SeatDTO(Integer seatId, String position, String seatType, boolean vip, Double unitPrice, boolean booked) {
        this.seatID = seatId;
        this.position = position;
        this.seatType = mapSeatType(seatType); // Thay đổi ở đây
        this.isVIP = vip;
        this.unitPrice = unitPrice;
        this.isBooked = booked;
    }

    private TypeOfSeat mapSeatType(String seatType) {
        if (seatType == null) {
            return null; // Hoặc giá trị mặc định
        }

        switch (seatType.toUpperCase()) { // Chuyển đổi chuỗi về chữ in hoa để ánh xạ chính xác
            case "SINGLE":
                return TypeOfSeat.Single;
            case "COUPLE":
                return TypeOfSeat.Couple;
            default:
                throw new IllegalArgumentException("Invalid TypeOfSeat value: " + seatType);
        }
    }


    private Seat_Status mapSeatStatus(String status) {
        if (status == null) {
            return null; // hoặc một giá trị mặc định
        }

        switch (status.toUpperCase()) {
            case "ACTIVE":
                return Seat_Status.Active;
            case "INACTIVE":
                return Seat_Status.Inactive;
            default:
                throw new IllegalArgumentException("Invalid Seat_Status value: " + status);
        }
    }


}