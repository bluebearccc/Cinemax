package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Integer id;
    private String email;
    private String password;
    private Role role;
    private Account_Status status;

    public AccountDTO(String email, String password, Role role, Account_Status status) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }
}
