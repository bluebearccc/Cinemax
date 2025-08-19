package com.bluebear.cinemax.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String gateway;

    // Sửa các dòng dưới đây
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "sub_account")
    private String subAccount;

    @Column(name = "amount_in")
    private Double amountIn;

    @Column(name = "amount_out")
    private Double amountOut;

    private Double accumulated;

    private String code;

    @Column(name = "transaction_content", columnDefinition = "TEXT")
    private String transactionContent;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}