package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Detail_FD;
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
    private Integer id;
    private Integer customerID;
    private Integer EmployeeID;
    private Integer promotionID;
    private LocalDateTime bookingDate;
    private Double totalPrice;

    private List<Detail_FDDTO> detail_FDDTO;

}
