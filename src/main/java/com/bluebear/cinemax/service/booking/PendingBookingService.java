package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.DetailSeatDTO;
import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.entity.Invoice;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PendingBookingService {
    @Autowired
    private BookingServiceImp bookingServiceImp;
    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private DetailSeatRepository detailSeatRepo;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void removeExpiredUnpaidSeats() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);

        List<Invoice> expiredInvoices = invoiceRepo.findByStatusAndBookingDateBefore(InvoiceStatus.Unpaid, threshold);

        for (Invoice invoice : expiredInvoices) {
            List<DetailSeat> unpaidSeats = detailSeatRepo.findByInvoiceInvoiceIDAndStatus(
                    invoice.getInvoiceID(), DetailSeat_Status.Unpaid
            );

            for (DetailSeat seat : unpaidSeats) {
                detailSeatRepo.delete(seat);
            }

            invoice.setStatus(InvoiceStatus.Cancelled); // Optional
            invoiceRepo.save(invoice);
        }
    }

}
