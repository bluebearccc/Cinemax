package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Invoice_Status;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    private Integer invoiceID;
    private Integer customerID;
    private Integer employeeID;
    private Integer promotionID;
    private Float discount;
    private LocalDateTime bookingDate;
    private Double totalPrice;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private Invoice_Status status;
}
