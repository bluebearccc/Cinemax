package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRespository extends JpaRepository<Employee, Integer> {
}
