package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.EmployeePosition;
import com.bluebear.cinemax.enums.TheaterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer employeeId;
    private EmployeePosition position;
    private TheaterStatus status;
    private Integer accountId;
    private Integer theaterId;
    private Integer adminId;
    private String fullName;
    private AccountDTO account;
    private TheaterDTO theater;
}
