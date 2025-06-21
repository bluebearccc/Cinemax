package com.bluebear.cinemax.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vnpay")

public class VnpayConfig {

    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;

}

