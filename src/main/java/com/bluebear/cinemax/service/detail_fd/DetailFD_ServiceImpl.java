package com.bluebear.cinemax.service.detail_fd;

import com.bluebear.cinemax.dto.Detail_FDDTO;
import com.bluebear.cinemax.entity.Detail_FD;
import com.bluebear.cinemax.repository.Detail_FDRepository;
import com.bluebear.cinemax.repository.InvoiceRepository; // Inject InvoiceRepository
import com.bluebear.cinemax.repository.TheaterStockRepository; // Inject TheaterStockRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetailFD_ServiceImpl implements DetaillFD_Service {

    @Autowired
    private Detail_FDRepository detail_FDRepository;

    @Autowired
    private InvoiceRepository invoiceRepository; // Inject InvoiceRepository

    @Autowired
    private TheaterStockRepository theaterStockRepository; // Inject TheaterStockRepository

    // Helper method to convert Detail_FD entity to Detail_FDDTO
    private Detail_FDDTO convertToDTO(Detail_FD detail_FD) {
        if (detail_FD == null) {
            return null;
        }
        return Detail_FDDTO.builder()
                .id(detail_FD.getId())
                .invoiceId(detail_FD.getInvoice() != null ? detail_FD.getInvoice().getInvoiceID() : null)
                .theaterStockId(detail_FD.getTheaterStock() != null ? detail_FD.getTheaterStock().getStockID() : null)
                .quantity(detail_FD.getQuantity())
                .totalPrice(detail_FD.getTotalPrice())
                // Populate the new fields
                .itemName(detail_FD.getTheaterStock() != null ? detail_FD.getTheaterStock().getItemName() : null)
                .bookingDate(detail_FD.getInvoice() != null ? detail_FD.getInvoice().getBookingDate() : null)
                .build();
    }

    // Helper method to convert Detail_FDDTO to Detail_FD entity
    private Detail_FD convertToEntity(Detail_FDDTO detail_FDDTO) {
        if (detail_FDDTO == null) {
            return null;
        }

        Detail_FD detail_FD = new Detail_FD();
        detail_FD.setId(detail_FDDTO.getId());
        detail_FD.setQuantity(detail_FDDTO.getQuantity());
        detail_FD.setTotalPrice(detail_FDDTO.getTotalPrice());

        // Set Invoice
        if (detail_FDDTO.getInvoiceId() != null) {
            invoiceRepository.findById(detail_FDDTO.getInvoiceId())
                    .ifPresent(detail_FD::setInvoice);
        } else {
            detail_FD.setInvoice(null);
        }

        // Set TheaterStock
        if (detail_FDDTO.getTheaterStockId() != null) {
            theaterStockRepository.findById(detail_FDDTO.getTheaterStockId())
                    .ifPresent(detail_FD::setTheaterStock);
        } else {
            detail_FD.setTheaterStock(null);
        }

        return detail_FD;
    }

    @Override
    public List<Detail_FDDTO> findByTheaterStockID(Integer id) {
        List<Detail_FD> details = detail_FDRepository.findAllByTheaterStock_TheaterStockId(id);
        return details.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

}