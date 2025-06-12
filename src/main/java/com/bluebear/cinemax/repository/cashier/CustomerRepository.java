package com.bluebear.cinemax.repository.cashier;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // Tìm khách hàng theo email
    Optional<Customer> findByAccount_Email(String email);

    // Tìm khách hàng theo số điện thoại
    Optional<Customer> findByPhone(String phone);

    // Tìm khách hàng theo tên (tìm kiếm gần đúng)
    List<Customer> findByFullNameContainingIgnoreCase(String fullName);

    // Tìm khách hàng có tài khoản active
    List<Customer> findByAccount_Status(Account.AccountStatus status);

    List<Customer> findByFullNameContainingOrPhoneContaining(String term, String term1);
}