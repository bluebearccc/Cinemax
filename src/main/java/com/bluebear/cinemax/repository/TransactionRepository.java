package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
