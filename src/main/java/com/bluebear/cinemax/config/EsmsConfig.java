package com.bluebear.cinemax.config;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix = "esms")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsmsConfig {
    private String apiKey;
    private String secretKey;
    private String brandname;
    private String url;

    // getters & setters

}
