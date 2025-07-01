package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    // Find account by email
    Optional<Account> findByEmail(String email);

    // Find accounts by role
    List<Account> findByRole(String role);

    // Find accounts by status
    List<Account> findByStatus(String status);

    // Find accounts by role and status
    List<Account> findByRoleAndStatus(String role, String status);

    // Search accounts by email containing keyword
    List<Account> findByEmailContainingIgnoreCase(String email);

    // Search with multiple criteria
    @Query("SELECT a FROM Account a WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:role IS NULL OR :role = '' OR a.role = :role) AND " +
            "(:status IS NULL OR :status = '' OR a.status = :status)")
    List<Account> findAccountsByCriteria(@Param("keyword") String keyword,
                                         @Param("role") String role,
                                         @Param("status") String status);

    // Count methods for statistics
    long countByStatus(String status);
    long countByRole(String role);

    // Custom queries for statistics
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'Active'")
    long countActiveAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'Banned'")
    long countBannedAccounts();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role = 'Customer'")
    long countCustomers();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.role = 'Admin'")
    long countAdmins();

    // Check if email exists
    boolean existsByEmail(String email);
}