package com.bluebear.cinemax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private Integer id;

    private Integer accountID;

    private String fullName;

    private String phone;

    public CustomerDTO(Integer accountID, String fullName, String phone) {
        this.accountID = accountID;
        this.fullName = fullName;
        this.phone = phone;
    }
}

