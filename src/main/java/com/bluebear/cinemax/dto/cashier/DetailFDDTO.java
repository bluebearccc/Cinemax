package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailFDDTO {
    private Integer id;
    private InvoiceDTO invoice;
    private TheaterDTO theaterStock;
    private Integer quantity;
    private Double totalPrice;
}