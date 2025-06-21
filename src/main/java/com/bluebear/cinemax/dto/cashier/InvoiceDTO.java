package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Integer invoiceId;
    private CustomerDTO customerId;
    private EmployeeDTO employeeId;
    private PromotionDTO promotionId;
    private Float discount;
    private LocalDateTime bookingDate;
    private Double totalPrice;
}