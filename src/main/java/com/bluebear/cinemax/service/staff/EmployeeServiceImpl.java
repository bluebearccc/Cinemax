package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.repository.staff.EmployeeRespository;
import com.bluebear.cinemax.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl {
    @Autowired
    private EmployeeRespository employeeRespository;
    public Employee getEmployeeById(Integer id) {
        return employeeRespository.findById(id).orElse(null);
    }
}
