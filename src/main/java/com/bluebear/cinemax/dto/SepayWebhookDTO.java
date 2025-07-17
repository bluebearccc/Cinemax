package com.bluebear.cinemax.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SepayWebhookDTO {
    @JsonProperty("id")
    private Integer sepayTransactionId;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String content;
    private String transferType;
    private Double transferAmount;
    private Double accumulated;
    private String code;
    private String referenceCode;
    private String description;
}