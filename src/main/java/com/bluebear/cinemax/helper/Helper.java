package com.bluebear.cinemax.helper;

import com.bluebear.cinemax.dto.cashier.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.cashier.*;
import com.bluebear.cinemax.service.cashier.CashierService;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

public class Helper {

    public static final String ROOM_TYPE_COUPLE = "COUPLE";
    public static final int NORMAL_SEAT_PRICE = 50000;
    public static final int VIP_SEAT_PRICE = 70000;
    public static final int COUPLE_SEAT_PRICE = 90000;

    public static final String ATTR_SELECTED_MOVIE = "selectedMovie";
    public static final String ATTR_SELECTED_SCHEDULE = "selectedSchedule";
    public static final String ATTR_SELECTED_SEATS = "selectedSeats";
    public static final String ATTR_CUSTOMER_INFO = "customerInfo";
    public static final String ATTR_PRICE_BREAKDOWN = "priceBreakdown";
    public static final String ATTR_CURRENT_STEP = "currentStep";
    public static final String ATTR_THEATER_ID = "theaterId";

    public static boolean isValidSession(HttpSession session) {
        return session.getAttribute(ATTR_THEATER_ID) != null &&
                session.getAttribute(ATTR_SELECTED_MOVIE) != null;
    }

    public static void clearSeatSelections(HttpSession session) {
        session.removeAttribute(ATTR_SELECTED_SEATS);
        session.removeAttribute(ATTR_CUSTOMER_INFO);
        session.removeAttribute(ATTR_PRICE_BREAKDOWN);
    }

    public static void clearSessionData(HttpSession session) {
        session.removeAttribute(ATTR_SELECTED_MOVIE);
        session.removeAttribute(ATTR_SELECTED_SCHEDULE);
        session.removeAttribute(ATTR_SELECTED_SEATS);
        session.removeAttribute(ATTR_CUSTOMER_INFO);
        session.removeAttribute(ATTR_PRICE_BREAKDOWN);
        session.removeAttribute(ATTR_CURRENT_STEP);
    }

    public static Map<String, Object> createScheduleInfo(String time, String roomName, String roomType, Integer scheduleId) {
        Map<String, Object> scheduleInfo = new HashMap<>();
        scheduleInfo.put("time", time);
        scheduleInfo.put("roomName", roomName);
        scheduleInfo.put("roomType", roomType);
        scheduleInfo.put("scheduleId", scheduleId);
        return scheduleInfo;
    }

    public static String normalizeSearchParam(String param) {
        if (param == null) return null;
        param = param.trim();
        return param.isEmpty() ? null : param;
    }

    public static List<String> getAvailableGenresForTheater(Integer theaterId) {
        return Arrays.asList("Hành động", "Hài", "Kinh dị", "Lãng mạn", "Khoa học viễn tưởng");
    }


    //===================SEAT===================================================================
    public static Map<String, Object> createSimpleSeatGrid(List<SeatDTO> seats, String roomType) {
        Map<String, Object> gridData = new HashMap<>();

        gridData.put("roomType", roomType);
        gridData.put("maxColumns", 0);
        gridData.put("totalRows", 0);

        if (seats == null || seats.isEmpty()) {
            gridData.put("rows", new ArrayList<>());
            return gridData;
        }

        seats = normalizeSeatsPosition(seats);

        Set<String> allRows = new TreeSet<>();
        Set<Integer> allColumns = new TreeSet<>();
        Map<String, SeatDTO> seatMap = new HashMap<>();

        for (SeatDTO seat : seats) {
            String position = seat.getPosition();
            if (position == null || position.isEmpty()) continue;

            try {
                String row = position.substring(0, 1);
                int col = Integer.parseInt(position.substring(1));

                allRows.add(row);
                allColumns.add(col);
                seatMap.put(position, seat);
            } catch (Exception e) {
                System.err.println("Error parsing seat position: " + position + " - " + e.getMessage());
            }
        }

        if (allRows.isEmpty() || allColumns.isEmpty()) {
            gridData.put("rows", new ArrayList<>());
            return gridData;
        }

        int maxColumns = Collections.max(allColumns);

        List<Map<String, Object>> gridRows = new ArrayList<>();

        for (String rowLabel : allRows) {
            Map<String, Object> rowData = new HashMap<>();
            rowData.put("rowLabel", rowLabel);

            List<Map<String, Object>> rowSeats = new ArrayList<>();

            for (int col = 1; col <= maxColumns; col++) {
                String seatPosition = rowLabel + col;
                SeatDTO seat = seatMap.get(seatPosition);

                Map<String, Object> seatData = new HashMap<>();

                if (seat != null) {
                    seatData.put("seatId", seat.getSeatId());
                    seatData.put("position", seatPosition);
                    seatData.put("isBooked", seat.getIsBooked());
                    seatData.put("isVIP", seat.getIsVIP());
                    seatData.put("unitPrice", seat.getUnitPrice());
                    seatData.put("exists", true);

                    String seatType = "NORMAL";
                    int price = NORMAL_SEAT_PRICE;

                    if (ROOM_TYPE_COUPLE.equalsIgnoreCase(roomType)) {
                        seatType = "COUPLE";
                        price = COUPLE_SEAT_PRICE;
                    } else if (seat.getIsVIP()) {
                        seatType = "VIP";
                        price = VIP_SEAT_PRICE;
                    }

                    seatData.put("seatType", seatType);
                    seatData.put("calculatedPrice", price);
                    seatData.put("cssClass", buildSeatCssClass(seat, roomType));
                } else {
                    seatData.put("exists", false);
                    seatData.put("cssClass", "seat-empty");
                    seatData.put("position", "");
                }

                rowSeats.add(seatData);
            }

            rowData.put("seats", rowSeats);
            gridRows.add(rowData);
        }

        gridData.put("rows", gridRows);
        gridData.put("maxColumns", maxColumns);
        gridData.put("totalRows", allRows.size());

        return gridData;
    }

    public static String normalizePosition(String position) {
        if (position == null || position.isEmpty()) return position;

        try {
            String row = position.substring(0, 1);
            int col = Integer.parseInt(position.substring(1));
            return row + col;
        } catch (Exception e) {
            System.err.println("Error normalizing position: " + position);
            return position;
        }
    }

    public static List<SeatDTO> normalizeSeatsPosition(List<SeatDTO> seats) {
        if (seats == null) return seats;

        for (SeatDTO seat : seats) {
            String normalized = normalizePosition(seat.getPosition());
            seat.setPosition(normalized);
        }

        return seats;
    }

    public static List<SeatDTO> getSeatsByIds(List<Integer> seatIds, SeatRepository seatRepository, CashierService cashierService) {
        List<Seat> seatEntities = seatRepository.findAllById(seatIds);
        return seatEntities.stream()
                .map(cashierService::convertToSeatDTO)
                .collect(Collectors.toList());
    }

    public static String buildSeatCssClass(SeatDTO seat, String roomType) {
        StringBuilder cssClass = new StringBuilder("seat");

        if (seat.getIsBooked()) {
            cssClass.append(" occupied");
        } else {
            cssClass.append(" available");
        }

        if (ROOM_TYPE_COUPLE.equalsIgnoreCase(roomType)) {
            cssClass.append(" couple");
        } else if (seat.getIsVIP()) {
            cssClass.append(" vip");
        } else {
            cssClass.append(" normal");
        }

        return cssClass.toString();
    }

}
