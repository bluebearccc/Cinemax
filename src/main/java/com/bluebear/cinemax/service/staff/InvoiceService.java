package com.bluebear.cinemax.service.staff;

import com.bluebear.cinemax.dto.InvoiceDTO;

public interface InvoiceService {
    public InvoiceDTO getInvoiceById(Integer id);
    public InvoiceDTO getInvoiceByDetailFDId(Integer id);
}
