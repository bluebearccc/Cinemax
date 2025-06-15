package com.bluebear.cinemax.service.employee;

import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.entity.Employee;

import java.util.List;

public interface EmployeeService {
    EmployeeDTO save(EmployeeDTO dto);

    EmployeeDTO findById(Integer id);

    List<EmployeeDTO> findAll();

    void deleteById(Integer id);

    EmployeeDTO findByAccountId(Integer accountId);

    // Optional: expose conversion methods
    Employee dtoToEntity(EmployeeDTO dto);

    EmployeeDTO entityToDto(Employee employee);
}
