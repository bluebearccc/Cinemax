package com.bluebear.cinemax.service.account;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.entity.Account;

import java.util.List;

public interface AccountService {

    AccountDTO save(AccountDTO dto);

    AccountDTO findById(Integer id);

    List<AccountDTO> findAll();

    void deleteById(Integer id);

    AccountDTO findAccountByEmail(String email);

    AccountDTO toDTO(Account account);

    Account toEntity(AccountDTO dto);
}
