package com.bluebear.cinemax.service.admin;

import com.bluebear.cinemax.entity.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<Customer> findAll();
    List<Customer> findByNameContaining(String keyword);
    List<Customer> getAllByOrderByFullNameAsc();
    List<Customer> getAllByOrderByFullNameDesc();
    Customer save(Customer customer);
    Optional<Customer> findById(Integer id);
    void deleteById(Integer id);
}