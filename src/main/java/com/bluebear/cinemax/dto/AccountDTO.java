package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enums.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Integer accountId;
    private String email;
    private String password;
    private AccountRole role;
    private AccountStatus status;
}