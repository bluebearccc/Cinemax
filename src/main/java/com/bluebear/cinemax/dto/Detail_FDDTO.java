package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Detail_FDDTO {
    private Integer id;
    private Integer invoiceId;
    private Integer theaterStockId;
    private Integer quantity;
    private Double totalPrice;
    private String itemName;
    private LocalDateTime bookingDate;

    public String getFormattedBookingDate() {
        return bookingDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
