package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Integer id;
    private String email;
    private String password;
    private Role role;
    private Account_Status status;
}
