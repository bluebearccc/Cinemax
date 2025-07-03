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
    @Column(name = "transaction_date") // Ánh xạ tới cột transaction_date
    private LocalDateTime transactionDate;

    @Column(name = "account_number") // Ánh xạ tới cột account_number
    private String accountNumber;

    @Column(name = "sub_account") // Ánh xạ tới cột sub_account
    private String subAccount;

    @Column(name = "amount_in") // Ánh xạ tới cột amount_in
    private Double amountIn;

    @Column(name = "amount_out") // Ánh xạ tới cột amount_out
    private Double amountOut;

    private Double accumulated;

    private String code;

    @Column(name = "transaction_content", columnDefinition = "TEXT") // Ánh xạ tới cột transaction_content
    private String transactionContent;

    @Column(name = "reference_number") // Ánh xạ tới cột reference_number
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at") // Ánh xạ tới cột created_at
    private LocalDateTime createdAt = LocalDateTime.now();
}