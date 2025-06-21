package com.bluebear.cinemax.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sepay")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SePayConfig {
    private String apiKey;
    private String secretKey;
    private String webhookEndpoint;

}
