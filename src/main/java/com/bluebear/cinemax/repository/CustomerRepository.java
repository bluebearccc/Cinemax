package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByPhone(String customerPhone);
    Optional<Customer> findByAccount_Id(Integer accountId);
    Optional<Customer> findByAccount_Email(String customerEmail);

    Customer findByAccount(Account account);
}
