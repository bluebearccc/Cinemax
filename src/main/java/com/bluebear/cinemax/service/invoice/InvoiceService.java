package com.bluebear.cinemax.service.invoice;

import com.bluebear.cinemax.dto.InvoiceDTO;
import com.bluebear.cinemax.entity.Invoice;

import java.util.List;

public interface InvoiceService {

    InvoiceDTO createInvoice(InvoiceDTO dto);

    InvoiceDTO getInvoiceById(Integer id);

    List<InvoiceDTO> getAllInvoices();

    InvoiceDTO updateInvoice(Integer id, InvoiceDTO dto);

    void deleteInvoice(Integer id);

    InvoiceDTO toDTO(Invoice invoice);

    Invoice toEntity(InvoiceDTO dto);
}
