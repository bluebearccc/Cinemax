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
//    public static DetailFDDTO fromEntity(DetailFD entity) {
//        if (entity == null) return null;
//
//        DetailFDDTO dto = new DetailFDDTO();
//        dto.setId(entity.getId());
//        dto.setInvoiceId(entity.getInvoice() != null ? entity.getInvoice().getInvoiceId() : null);
//        dto.setTheaterStockId(entity.getTheaterStock() != null ? entity.getTheaterStock().getTheaterStockID() : null);
//        dto.setQuantity(entity.getQuantity());
//        dto.setTotalPrice(entity.getTotalPrice());
//        dto.setStatus(entity.getStatus());
//        return dto;
//    }

}
