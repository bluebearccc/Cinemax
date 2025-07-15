package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.InvoiceStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDetailDTO {
    private Integer invoiceId;
    private String theaterName;
    private List<String> foodName;
    private String roomName;
    private LocalDateTime bookingDate;
    private List<String> seats;
    private List<String> theaterstock;
    private double totalPrice;
    private double discount;
    private InvoiceStatus status;
}
