package com.bluebear.cinemax.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.sepay")
@Getter
@Setter
public class sepayConfig {
    private String account;
    private String bank;
}
