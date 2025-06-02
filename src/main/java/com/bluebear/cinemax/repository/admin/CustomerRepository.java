package com.bluebear.cinemax.repository.admin;

import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    public List<Customer> findByFullName(String fullName);

    public List<Customer> findAll();

    public List<Customer> findByFullNameContaining(String keyword);

    public List<Customer> getAllByOrderByFullNameAsc();

    public List<Customer> getAllByOrderByFullNameDesc();
}