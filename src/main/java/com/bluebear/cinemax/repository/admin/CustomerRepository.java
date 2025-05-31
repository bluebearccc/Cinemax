package com.bluebear.cinemax.repository.admin;

import com.bluebear.cinemax.entity.Customer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Modifying
    @Transactional
    @Query("update Account a SET a.status = true where a.id = :id")
    void banAccount(int id);

    @Modifying
    @Transactional
    @Query("update Account a SET a.status = false where a.id = :id")
    void unbanAccount(int id);
}
