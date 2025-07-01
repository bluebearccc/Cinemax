package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Employee_Status;
import com.bluebear.cinemax.enumtype.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeDTO {
    private Integer id;
    private Role position;
    private Employee_Status status;
    private Integer accountId;
    private Integer theaterId;
    private Integer adminId;
    private String fullName;
}
