package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}
