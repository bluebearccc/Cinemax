package com.bluebear.cinemax.repository;
import com.bluebear.cinemax.entity.Detail_FD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailFDRepository extends JpaRepository<Detail_FD, Integer> {
    List<Detail_FD> findByInvoiceInvoiceID(Integer invoiceId);
}
