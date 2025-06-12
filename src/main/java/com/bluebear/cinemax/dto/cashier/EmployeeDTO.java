package com.bluebear.cinemax.dto.cashier;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer employeeId;
    private String position;
    private String status;
    private AccountDTO account;
    private TheaterDTO theaterId;
    private EmployeeDTO admin;
    private String fullName;
}
