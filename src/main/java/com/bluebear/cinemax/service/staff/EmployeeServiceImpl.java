package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Account; // Assuming you have an Account entity
import com.bluebear.cinemax.entity.Theater; // Assuming you have a Theater entity
import com.bluebear.cinemax.repository.AccountRepository; // Assuming you have an AccountRepository
import com.bluebear.cinemax.repository.EmployeeRepository;
import com.bluebear.cinemax.repository.TheaterRepository; // Assuming you have a TheaterRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService { // Implement EmployeeService

    @Autowired
    private EmployeeRepository employeeRepository; // Corrected repository name

    @Autowired
    private AccountRepository accountRepository; // Inject AccountRepository

    @Autowired
    private TheaterRepository theaterRepository; // Inject TheaterRepository

    // Helper method to convert Employee entity to EmployeeDTO
    private EmployeeDTO convertToDTO(Employee employee) {
        if (employee == null) {
            return null;
        }
        return EmployeeDTO.builder()
                .id(employee.getId())
                .position(employee.getPosition())
                .status(employee.getStatus())
                .accountId(employee.getAccount() != null ? employee.getAccount().getId() : null)
                .theaterId(employee.getTheater() != null ? employee.getTheater().getTheaterID() : null)
                .adminId(employee.getAdmin() != null ? employee.getAdmin().getId() : null) // Assuming 'admin' is an Employee itself
                .fullName(employee.getFullName())
                .build();
    }

    // Helper method to convert EmployeeDTO to Employee entity
    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setId(employeeDTO.getId());
        employee.setPosition(employeeDTO.getPosition());
        employee.setStatus(employeeDTO.getStatus());
        employee.setFullName(employeeDTO.getFullName());

        // Set Account
        if (employeeDTO.getAccountId() != null) {
            accountRepository.findById(employeeDTO.getAccountId())
                    .ifPresent(employee::setAccount);
        } else {
            employee.setAccount(null);
        }

        // Set Theater
        if (employeeDTO.getTheaterId() != null) {
            theaterRepository.findById(employeeDTO.getTheaterId())
                    .ifPresent(employee::setTheater);
        } else {
            employee.setTheater(null);
        }

        // Set Admin (assuming admin is another Employee)
        if (employeeDTO.getAdminId() != null) {
            employeeRepository.findById(employeeDTO.getAdminId())
                    .ifPresent(employee::setAdmin);
        } else {
            employee.setAdmin(null);
        }

        return employee;
    }

    @Override
    public EmployeeDTO getEmployeeById(Integer id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);
        return employeeOptional.map(this::convertToDTO).orElse(null);
    }


}