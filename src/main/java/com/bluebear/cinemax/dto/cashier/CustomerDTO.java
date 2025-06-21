package com.bluebear.cinemax.dto.cashier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {
    private Integer customerId;
    private AccountDTO accountId;
    private String fullName;
    private String phone;
    private Integer point;
}
