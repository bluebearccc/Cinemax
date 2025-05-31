package com.bluebear.cinemax.service.admin;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.admin.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void banAccount(int id) {
        customerRepository.banAccount(id);
    }

    public void unbanAccount(int id) {
        customerRepository.unbanAccount(id);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }
}
