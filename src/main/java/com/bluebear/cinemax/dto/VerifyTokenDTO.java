package com.bluebear.cinemax.dto;

import jakarta.persistence.Column;
import lombok.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyTokenDTO {

    private Integer id;

    private String email;

    private String token;

    private Date expiresAt;

    private String password;

    private String fullName;
}
