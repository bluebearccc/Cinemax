package com.bluebear.cinemax.service.admin;

import com.bluebear.cinemax.entity.Account;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    Account save(Account account);
    Optional<Account> findById(Integer id);
    Optional<Account> findByEmail(String email);
    List<Account> findAll();
    void deleteById(Integer id);
    void updateStatus(Integer id, boolean status);
}