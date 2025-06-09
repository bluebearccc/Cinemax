package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.Employee_Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private int id;
    private String fullName;
    private String position;
    private Employee_Status status;
    private String accountID;
    private String theaterID;
    private String adminID;
}
