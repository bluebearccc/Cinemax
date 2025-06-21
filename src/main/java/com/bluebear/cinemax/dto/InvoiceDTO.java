package com.bluebear.cinemax.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InvoiceDTO {

    private Integer invoiceId;
    private Integer customerId;
    private Integer employeeId;
    private Integer promotionId;
    private Double discount;
    private LocalDateTime bookingDate;
    private BigDecimal totalprice;
}
