package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailSeatDTO {
    private Integer id;
    private InvoiceDTO invoiceId;       // Thay vì entity Invoice
    private SeatDTO seat;          // Thay vì entity Seat
    private ScheduleDTO schedule;      // Thay vì entity Schedule
}
