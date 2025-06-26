package com.bluebear.cinemax.service;

import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public CustomerDTO getCustomerByPhone(String customerPhone) {
        Optional<Customer> customerOptional = customerRepository.findByPhone(customerPhone);
        return customerOptional
                .map(this::toDto)
                .orElse(null);
    }

    private CustomerDTO toDto(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .accountID(customer.getAccount().getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .build();
    }

    public CustomerDTO getCustomerByEmail(String customerEmail) {
        return customerRepository.findByAccount_Email(customerEmail)
                .map(this::toDto)
                .orElse(null);
    }
}
