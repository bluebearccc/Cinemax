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

    public EmployeeDTO(Role position, Employee_Status status, Integer accountId, Integer theaterId, Integer adminId, String fullName) {
        this.position = position;
        this.status = status;
        this.accountId = accountId;
        this.theaterId = theaterId;
        this.adminId = adminId;
        this.fullName = fullName;
    }
}
