package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.DetailFD;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetailFDDTO {
    private Integer id;
    private Integer invoiceId;
    private Integer theaterStockId;
    private Integer quantity;
    private Double totalPrice;
    private String status;


}
