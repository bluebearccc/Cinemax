package com.bluebear.cinemax.repository.admin;

import com.bluebear.cinemax.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    @Modifying
    @Query("UPDATE Account a SET a.status = :status WHERE a.id = :accountID")
    void updateAccountStatus(@Param("accountID") Integer accountID, @Param("status") boolean status);
}
