package com.bluebear.cinemax.service.employee;

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
public class EmployeeServiceImpl implements EmployeeService{

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TheaterRepository theaterRepository;


    // Convert entity → DTO
     public EmployeeDTO toDTO(Employee employee) {
        if (employee == null) return null;

        Integer accountId = employee.getAccount() != null ? employee.getAccount().getId() : null;
        Integer theaterId = employee.getTheater() != null ? employee.getTheater().getTheaterID() : null;
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
    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) return null;

        Optional<Account> accountOpt = accountRepository.findById(dto.getAccountId());
        Optional<Theater> theaterOpt = theaterRepository.findById(dto.getTheaterId());
        Optional<Employee> adminOpt = dto.getAdminId() != null ? employeeRepository.findById(dto.getAdminId()) : null;

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

    @Override
    public EmployeeDTO getEmployeeById(int i) {
        return employeeRepository.findById(i).get() != null ? toDTO(employeeRepository.findById(i).get()) : null;
    }

    public EmployeeDTO save(EmployeeDTO dto) {
        Employee entity = toEntity(dto);
        Employee saved = employeeRepository.save(entity);
        return toDTO(saved);
    }

    public EmployeeDTO findById(Integer id) {
        Optional<Employee> optional = employeeRepository.findById(id);
        return optional.map(this::toDTO).orElse(null);
    }

    public List<EmployeeDTO> findAll() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDTO> dtos = new ArrayList<>();
        for (Employee e : employees) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    public EmployeeDTO findByAccountId(Integer accountId) {
        Optional<Employee> optional = employeeRepository.findByAccount_Id(accountId);
        return optional.map(this::toDTO).orElse(null);
    }
}
