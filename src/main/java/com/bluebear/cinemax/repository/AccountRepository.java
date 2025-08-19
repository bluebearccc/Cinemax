package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail(String email);

    @Query(value = "SELECT DISTINCT a FROM Account a " +
            "LEFT JOIN FETCH a.customer " +
            "LEFT JOIN FETCH a.employee " +
            "WHERE (:keyWord IS NULL OR a.email LIKE %:keyWord%) " +
            "AND (:role IS NULL OR a.role = :role) " +
            "AND (:status IS NULL OR a.status = :status)",
            countQuery = "SELECT count(a) FROM Account a WHERE " +
                    "(:keyWord IS NULL OR a.email LIKE %:keyWord%) " +
                    "AND (:role IS NULL OR a.role = :role) " +
                    "AND (:status IS NULL OR a.status = :status)")
    Page<Account> searchAccounts(@Param("keyWord") String keyWord,
                                 @Param("role") Role role,
                                 @Param("status") Account_Status status,
                                 Pageable pageable);
}