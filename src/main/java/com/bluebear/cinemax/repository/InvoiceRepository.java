package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.entity.Invoice;

import com.bluebear.cinemax.enumtype.Invoice_Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByCustomerAndStatus(Customer customer, Invoice_Status status);
    boolean existsByCustomerAndStatus(Customer customer, Invoice_Status status);
    Optional<Invoice> findByInvoiceIdAndStatus(int invoiceId, Invoice_Status status);

}
