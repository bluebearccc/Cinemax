package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Invoice_Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceDTO {
    private Integer invoiceID;
    private Integer customerID;
    private Integer employeeID;
    private Integer promotionID;
    private Float discount;
    private LocalDateTime bookingDate;
    private Double totalPrice;
    private List<Detail_FDDTO> detail_FDDTO;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private Invoice_Status status;
}
