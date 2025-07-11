package com.bluebear.cinemax.dto;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private Integer invoiceId;
    private Integer customerId;
    private Integer employeeId;
    private Integer promotionId;
    private Double discount;
    private LocalDateTime bookingDate;
    private Double totalPrice;
    private InvoiceStatus status;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private List<DetailSeatDTO> detailSeats;
    private List<Detail_FDDTO> detail_FDDTO;
}