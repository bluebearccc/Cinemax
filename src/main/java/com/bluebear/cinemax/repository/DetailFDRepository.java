package com.bluebear.cinemax.repository;

import com.bluebear.cinemax.entity.Detail_FD;
import com.bluebear.cinemax.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailFDRepository extends JpaRepository<Detail_FD, Integer> {
    @Query("SELECT df FROM Detail_FD df WHERE df.invoice = :invoice")
    List<Detail_FD> findByInvoice(Invoice invoice);
}