package com.bluebear.cinemax.dto;
import com.bluebear.cinemax.enumtype.Seat_Status;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SeatUpdateRequest {
    private Integer roomId;
    private List<Integer> vipSeatIds;
    private Double nonVipPrice;
    private Double vipPrice;
    private Map<Integer, String> positions;
    private Map<Integer, Seat_Status> seatStatuses;
}