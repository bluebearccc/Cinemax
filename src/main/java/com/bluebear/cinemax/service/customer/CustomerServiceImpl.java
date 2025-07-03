package com.bluebear.cinemax.service.customer;

import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.AccountRepository;
import com.bluebear.cinemax.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;

    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setAccountID(customer.getAccount() != null ? customer.getAccount().getId() : null);
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());
        dto.setPoint(customer.getPoint());
        return dto;
    }

    public Customer toEntity(CustomerDTO dto) {
        if (dto == null) return null;

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());
        if (dto.getAccountID() != null) {
            Optional<Account> optional = accountRepository.findById(dto.getAccountID());
            optional.ifPresent(customer::setAccount);
        }
        customer.setPoint(dto.getPoint());
        return customer;
    }

    public CustomerDTO save(CustomerDTO dto) {
        Customer entity = toEntity(dto);
        Customer saved = customerRepository.save(entity);
        return toDTO(saved);
    }

    public CustomerDTO findById(Integer id) {
        return customerRepository.findById(id)
                .map(this::toDTO)
                .orElse(null);
    }

    public List<CustomerDTO> findAll() {
        List<CustomerDTO> dtos = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            dtos.add(toDTO(c));
        }
        return dtos;
    }

    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    public CustomerDTO getUserByAccountID(Integer accountId) {
        return customerRepository.findByAccount_Id(accountId)
                .map(this::toDTO)
                .orElse(null);
    }

    public CustomerDTO getCustomerByEmail(String customerEmail) {
        return customerRepository.findByAccount_Email(customerEmail)
                .map(this::toDTO)
                .orElse(null);
    }
}