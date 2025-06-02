package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Employee;
import com.bluebear.cinemax.entity.Theater;
import com.bluebear.cinemax.repository.AccountRepository;
import com.bluebear.cinemax.repository.EmployeeRepository;
import com.bluebear.cinemax.repository.TheaterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final TheaterRepository theaterRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           AccountRepository accountRepository,
                           TheaterRepository theaterRepository) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.theaterRepository = theaterRepository;
    }

    // Convert entity → DTO
    private EmployeeDTO entityToDto(Employee employee) {
        if (employee == null) return null;

        Integer accountId = employee.getAccount() != null ? employee.getAccount().getId() : null;
        Integer theaterId = employee.getTheater() != null ? employee.getTheater().getId() : null;
        Integer adminId = employee.getAdmin() != null ? employee.getAdmin().getId() : null;

        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setPosition(employee.getPosition());
        dto.setStatus(employee.getStatus());
        dto.setAccountId(accountId);
        dto.setTheaterId(theaterId);
        dto.setAdminId(adminId);
        dto.setFullName(employee.getFullName());

        return dto;
    }

    // Convert DTO → entity
    private Employee dtoToEntity(EmployeeDTO dto) {
        if (dto == null) return null;

        Optional<Account> accountOpt = accountRepository.findById(dto.getAccountId());
        Optional<Theater> theaterOpt = theaterRepository.findById(dto.getTheaterId());
        Optional<Employee> adminOpt = dto.getAdminId() != null ? employeeRepository.findById(dto.getAdminId()) : Optional.empty();

        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setPosition(dto.getPosition());
        employee.setStatus(dto.getStatus());
        employee.setFullName(dto.getFullName());

        accountOpt.ifPresent(employee::setAccount);
        theaterOpt.ifPresent(employee::setTheater);
        adminOpt.ifPresent(employee::setAdmin);

        return employee;
    }

    public EmployeeDTO save(EmployeeDTO dto) {
        Employee entity = dtoToEntity(dto);
        Employee saved = employeeRepository.save(entity);
        return entityToDto(saved);
    }

    public EmployeeDTO findById(Integer id) {
        Optional<Employee> optional = employeeRepository.findById(id);
        return optional.map(this::entityToDto).orElse(null);
    }

    public List<EmployeeDTO> findAll() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> dtos = new ArrayList<>();
        for (Employee e : employees) {
            dtos.add(entityToDto(e));
        }
        return dtos;
    }

    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    public EmployeeDTO findByAccountId(Integer accountId) {
        Optional<Employee> optional = employeeRepository.findByAccount_Id(accountId);
        return optional.map(this::entityToDto).orElse(null);
    }
}
