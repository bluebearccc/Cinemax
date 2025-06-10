package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    // Tìm tài khoản theo email
    Optional<Account> findByEmail(String email);

    // Tìm tài khoản active
    List<Account> findByStatus(Account.AccountStatus status);

    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);
}
