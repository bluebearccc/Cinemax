package com.bluebear.cinemax.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordDTO {
    private Integer id;
    private Integer otp;
    private Integer accountId;
    private LocalDateTime expiryDate;
}
