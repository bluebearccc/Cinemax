package com.bluebear.cinemax.dto.cashier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PriceBreakdownDTO {
    private String movieName;
    private String scheduleTime;
    private String roomName;
    private String roomType;
    private List<String> selectedSeats;
    private List<SeatPriceInfo> seatPrices;
    private List<FoodItem> foodItems;
    private Integer totalTicketPrice;
    private Integer totalFoodPrice;
    private Integer total;
    private Integer coupleSeatsCount;
    private Integer vipSeatsCount;
    private Integer normalSeatsCount;

    // Constructors
    public PriceBreakdownDTO() {
        this.selectedSeats = new ArrayList<>();
        this.seatPrices = new ArrayList<>();
        this.foodItems = new ArrayList<>();
        this.totalTicketPrice = 0;
        this.totalFoodPrice = 0;
        this.total = 0;
        this.coupleSeatsCount = 0;
        this.vipSeatsCount = 0;
        this.normalSeatsCount = 0;
    }

    public PriceBreakdownDTO(MovieDTO movie, List<String> selectedSeats,
                             Map<String, Object> schedule, Map<String, Integer> foodQuantities) {
        this();
        this.movieName = movie.getMovieName();
        this.scheduleTime = (String) schedule.get("time");
        this.roomName = (String) schedule.get("roomName");
        this.roomType = (String) schedule.get("roomType");
        this.selectedSeats = new ArrayList<>(selectedSeats);

        // Calculate seat prices based on room type and seat count
        calculateSeatPrices();
    }

    // Inner classes for structured data
    public static class SeatPriceInfo {
        private String seatType;
        private Integer quantity;
        private Integer unitPrice;
        private Integer totalPrice;

        public SeatPriceInfo() {}

        public SeatPriceInfo(String seatType, Integer quantity, Integer unitPrice) {
            this.seatType = seatType;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }

        // Getters and Setters
        public String getSeatType() { return seatType; }
        public void setSeatType(String seatType) { this.seatType = seatType; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Integer getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }

        public Integer getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    }

    public static class FoodItem {
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private Integer totalPrice;

        public FoodItem() {}

        public FoodItem(String name, Integer quantity, BigDecimal unitPrice) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)).intValue();
        }

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public Integer getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Integer totalPrice) { this.totalPrice = totalPrice; }
    }

    // Method to calculate seat prices
    private void calculateSeatPrices() {
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            return;
        }

        // Constants for seat prices (should match controller)
        final int COUPLE_SEAT_PRICE = 190000;
        final int VIP_SEAT_PRICE = 95000;
        final int NORMAL_SEAT_PRICE = 75000;

        if ("Couple".equalsIgnoreCase(roomType)) {
            // All seats in couple room are couple seats
            coupleSeatsCount = selectedSeats.size();
            seatPrices.add(new SeatPriceInfo("Ghế đôi", coupleSeatsCount, COUPLE_SEAT_PRICE));
            totalTicketPrice = coupleSeatsCount * COUPLE_SEAT_PRICE;
        } else {
            // For regular rooms, assume all seats are normal for now
            // In a real implementation, you would need to check each seat's VIP status
            normalSeatsCount = selectedSeats.size();
            seatPrices.add(new SeatPriceInfo("Ghế thường", normalSeatsCount, NORMAL_SEAT_PRICE));
            totalTicketPrice = normalSeatsCount * NORMAL_SEAT_PRICE;
        }

        total = totalTicketPrice + totalFoodPrice;
    }

    // Method to recalculate total when food is added
    public void recalculateTotal() {
        totalFoodPrice = foodItems.stream()
                .mapToInt(FoodItem::getTotalPrice)
                .sum();
        total = totalTicketPrice + totalFoodPrice;
    }

    // Getters and Setters
    public String getMovieName() { return movieName; }
    public void setMovieName(String movieName) { this.movieName = movieName; }

    public String getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(String scheduleTime) { this.scheduleTime = scheduleTime; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public List<String> getSelectedSeats() { return selectedSeats; }
    public void setSelectedSeats(List<String> selectedSeats) { this.selectedSeats = selectedSeats; }

    public List<SeatPriceInfo> getSeatPrices() { return seatPrices; }
    public void setSeatPrices(List<SeatPriceInfo> seatPrices) { this.seatPrices = seatPrices; }

    public List<FoodItem> getFoodItems() { return foodItems; }
    public void setFoodItems(List<FoodItem> foodItems) { this.foodItems = foodItems; }

    public Integer getTotalTicketPrice() { return totalTicketPrice; }
    public void setTotalTicketPrice(Integer totalTicketPrice) { this.totalTicketPrice = totalTicketPrice; }

    public Integer getTotalFoodPrice() { return totalFoodPrice; }
    public void setTotalFoodPrice(Integer totalFoodPrice) { this.totalFoodPrice = totalFoodPrice; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getCoupleSeatsCount() { return coupleSeatsCount; }
    public void setCoupleSeatsCount(Integer coupleSeatsCount) { this.coupleSeatsCount = coupleSeatsCount; }

    public Integer getVipSeatsCount() { return vipSeatsCount; }
    public void setVipSeatsCount(Integer vipSeatsCount) { this.vipSeatsCount = vipSeatsCount; }

    public Integer getNormalSeatsCount() { return normalSeatsCount; }
    public void setNormalSeatsCount(Integer normalSeatsCount) { this.normalSeatsCount = normalSeatsCount; }

}