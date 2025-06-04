package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Integer invoiceId;
    private Integer customerId;
    private Integer employeeId;
    private Float discount;
    private LocalDateTime bookingDate;
    private BigDecimal totalPrice;
    private CustomerDTO customer;
    private EmployeeDTO employee;
    private List<DetailSeatDTO> detailSeats;
    private List<DetailFDDTO> detailFDs;
}