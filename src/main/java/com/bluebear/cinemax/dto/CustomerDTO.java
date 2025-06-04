package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Integer customerId;
    private Integer accountId;
    private String fullName;
    private String phone;
    private Integer point;
    private AccountDTO account;
}
