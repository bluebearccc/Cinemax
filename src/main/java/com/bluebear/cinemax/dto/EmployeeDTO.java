package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Employee_Status;
import com.bluebear.cinemax.enumtype.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Integer id;
    private Role position;
    private Employee_Status status;
    private Integer accountId;
    private Integer theaterId;
    private Integer adminId;
    private String fullName;
    private List<InvoiceDTO> invoiceDTOList;
}
