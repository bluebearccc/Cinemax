package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByAccount_Id(Integer accountId);

}
