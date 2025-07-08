package com.bluebear.cinemax.dto;

import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Integer id;

    @NotEmpty(message = "Email không được để trống.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    private String password;

    @NotNull(message = "Vui lòng chọn vai trò.")
    private Role role;

    @NotNull(message = "Vui lòng chọn trạng thái.")
    private Account_Status status;
}