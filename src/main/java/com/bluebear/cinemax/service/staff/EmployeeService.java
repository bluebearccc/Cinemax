package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.entity.Employee;

public interface EmployeeService {
    public EmployeeDTO getEmployeeById(Integer id);

}
