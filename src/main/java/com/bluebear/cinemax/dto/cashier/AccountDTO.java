package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Integer accountId;
    private String email;
    private String password;
    private String role;   // Enum dưới dạng String cho dễ xử lý phía frontend hoặc REST API
    private String status; // Enum dưới dạng String
}