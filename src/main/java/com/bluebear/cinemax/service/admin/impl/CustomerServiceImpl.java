package com.bluebear.cinemax.service.admin.impl;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.admin.CustomerRepository;
import com.bluebear.cinemax.service.admin.CustomerService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> findByNameContaining(String keyword) {
        return customerRepository.findByFullNameContaining(keyword);
    }

    @Override
    public List<Customer> getAllByOrderByFullNameAsc() {
        return customerRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
    }

    @Override
    public List<Customer> getAllByOrderByFullNameDesc() {
        return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "fullName"));
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public Customer findCustomerById(Integer id) {
        return customerRepository.findCustomerByCustomerID(id);
    }

    @Override
    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    @Override
    public List<Customer> findByNameContainingOrderByFullNameAsc(String keyword) {
        return customerRepository.findByFullNameContainingOrderByFullNameAsc(keyword);
    }

    @Override
    public List<Customer> findByNameContainingOrderByFullNameDesc(String keyword) {
        return customerRepository.findByFullNameContainingOrderByFullNameDesc(keyword);
    }

}