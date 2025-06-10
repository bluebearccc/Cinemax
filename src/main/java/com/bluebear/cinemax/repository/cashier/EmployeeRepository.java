package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // Tìm nhân viên theo tài khoản
    Optional<Employee> findByAccount_AccountId(Integer accountId);

    // Tìm nhân viên theo rạp và chức vụ
    List<Employee> findByTheater_TheaterIdAndPositionAndStatus(Integer theaterId, Employee.Position position, Employee.EmployeeStatus status);

    // Tìm thu ngân trong rạp
    List<Employee> findByTheater_TheaterIdAndPositionInAndStatus(Integer theaterId, List<Employee.Position> positions, Employee.EmployeeStatus status);
}
