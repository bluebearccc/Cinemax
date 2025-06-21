package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByAccount_Email(String accountEmail);

}