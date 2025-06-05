package com.bluebear.cinemax.repository.admin;

import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    public List<Customer> findByFullName(String fullName);

    public Customer findCustomerByCustomerID(Integer customerID);

    public List<Customer> findAll();

    public List<Customer> findByFullNameContaining(String keyword);

    public List<Customer> getAllByOrderByFullNameAsc();

    public List<Customer> getAllByOrderByFullNameDesc();

    public List<Customer> findByFullNameContainingOrderByFullNameAsc(String keyword);

    public List<Customer> findByFullNameContainingOrderByFullNameDesc(String keyword);
}