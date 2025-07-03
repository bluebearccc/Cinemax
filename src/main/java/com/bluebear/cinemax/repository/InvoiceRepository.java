package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByCustomerAndStatus(Customer customer, InvoiceStatus invoiceStatus);

    boolean existsByCustomerAndStatus(Customer customer, InvoiceStatus invoiceStatus);
}
