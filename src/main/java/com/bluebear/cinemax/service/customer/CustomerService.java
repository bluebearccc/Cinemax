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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    private CustomerDTO entityToDto(Customer customer) {
        if (customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setAccountID(customer.getAccount() != null ? customer.getAccount().getId() : null);
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());

        return dto;
    }

    private Customer dtoToEntity(CustomerDTO dto) {
        if (dto == null) return null;

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());

        if (dto.getAccountID() != null) {
            Optional<Account> optional = accountRepository.findById(dto.getAccountID());
            optional.ifPresent(customer::setAccount);
        }

        return customer;
    }

    public CustomerDTO save(CustomerDTO dto) {
        Customer entity = dtoToEntity(dto);
        Customer saved = customerRepository.save(entity);
        return entityToDto(saved);
    }

    public CustomerDTO findById(Integer id) {
        return customerRepository.findById(id)
                .map(this::entityToDto)
                .orElse(null);
    }

    public List<CustomerDTO> findAll() {
        List<CustomerDTO> dtos = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            dtos.add(entityToDto(c));
        }
        return dtos;
    }

    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    public CustomerDTO getUserByAccountID(Integer accountId) {
        return customerRepository.findByAccount_Id(accountId)
                .map(this::entityToDto)
                .orElse(null);
    }
}