package com.bluebear.cinemax.service.account;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void updateAccount(AccountDTO accountDTO) {
        Account existingAccount = accountRepository.findById(accountDTO.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản với ID: " + accountDTO.getId()));

        if (existingAccount.getRole() == Role.Customer) {
            existingAccount.setStatus(accountDTO.getStatus());
        } else {
            existingAccount.setEmail(accountDTO.getEmail());
            existingAccount.setRole(accountDTO.getRole());
            existingAccount.setStatus(accountDTO.getStatus());

            if (accountDTO.getPassword() != null && !accountDTO.getPassword().isEmpty()) {
                existingAccount.setPassword(accountDTO.getPassword());
            }
        }

        accountRepository.save(existingAccount);
    }

    public Account toEntity(AccountDTO dto) {
        if (dto == null) return null;
        Account account = new Account();
        account.setId(dto.getId());
        account.setEmail(dto.getEmail());
        account.setPassword(dto.getPassword());
        account.setRole(dto.getRole());
        account.setStatus(dto.getStatus());
        return account;
    }


    public AccountDTO toDTO(Account account) {
        if (account == null) return null;
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setEmail(account.getEmail());
        dto.setPassword(account.getPassword());
        dto.setRole(account.getRole());
        dto.setStatus(account.getStatus());
        return dto;
    }

    public AccountDTO save(AccountDTO dto) {
        Account entity = toEntity(dto);
        Account saved = accountRepository.save(entity);
        return toDTO(saved);
    }

    public AccountDTO findById(Integer id) {
        Optional<Account> optional = accountRepository.findById(id);
        if (optional.isPresent()) return toDTO(optional.get());
        else return null;
    }

    public List<AccountDTO> findAll() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDTO> dtos = new ArrayList<>();

        for (Account account : accounts) {
            AccountDTO dto = toDTO(account);
            dtos.add(dto);
        }

        return dtos;
    }

    public void deleteById(Integer id) {
        accountRepository.deleteById(id);
    }

    public AccountDTO findAccountByEmail(String email) {
        Optional<Account> optional = accountRepository.findByEmail(email);

        if (optional.isPresent()) {
            Account account = optional.get();
            AccountDTO dto = new AccountDTO();

            dto.setId(account.getId());
            dto.setEmail(account.getEmail());
            dto.setPassword(account.getPassword());
            dto.setRole(account.getRole());
            dto.setStatus(account.getStatus());
            return dto;
        } else {
            return null;
        }
    }


    @Override
    public Page<AccountDTO> searchAccounts(String keyWord, String roleStr, String statusStr, int pageNo, int pageSize, String sort) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by(sort));

        Role role = null;
        if (roleStr != null && !roleStr.isEmpty() && !roleStr.equalsIgnoreCase("All")) {
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException ignored) {}
        }

        Account_Status status = null;
        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equalsIgnoreCase("All")) {
            try {
                status = Account_Status.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) {}
        }

        String finalKeyword = (keyWord != null && !keyWord.trim().isEmpty()) ? keyWord.trim() : null;

        Page<Account> accountPage = accountRepository.searchAccounts(finalKeyword, role, status, pageable);

        return accountPage.map(this::toDTO);
    }

    @Override
    public void saveAccount(AccountDTO accountDTO) {
        accountRepository.save(toEntity(accountDTO));
    }
}