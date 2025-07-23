package com.bluebear.cinemax.security;

import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountDetailService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElse(null);
        if (account == null) {
            System.out.println("AccountDetailService.loadUserByUsername() - account is null: " + username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return User.withUsername(account.getEmail()).
                                password(account.getPassword()).
                                authorities(account.getRole().name()).
                                accountLocked(account.getStatus().name().equals(Account_Status.Banned.name()))
                                .build();
    }
}
