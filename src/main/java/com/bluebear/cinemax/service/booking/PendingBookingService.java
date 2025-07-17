package com.bluebear.cinemax.service.booking;

import com.bluebear.cinemax.dto.DetailSeatDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.DetailSeatRepository;
import com.bluebear.cinemax.repository.FeedbackServiceRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PendingBookingService {
    @Autowired
    private FeedbackServiceRepository feedbackServiceRepository;
    @Autowired
    private InvoiceRepository invoiceRepo;

    @Autowired
    private DetailSeatRepository detailSeatRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void removeExpiredUnpaidSeats() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);

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
    //sau 1h khi phim kết thúc sẽ gửi mail
    @Scheduled(fixedRate = 30 * 60 * 1000) // mỗi 30 phút
    @Transactional
    public void sendFeedbackForms() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusHours(2);
        LocalDateTime end = now.minusHours(1);

        List<Invoice> invoices = invoiceRepository.findInvoicesInFeedbackWindow(start, end);

        for (Invoice invoice : invoices) {
            Customer customer = invoice.getCustomer();
            if (customer == null || customer.getAccount() == null) continue; // kiểm tra null

            Schedule schedule = invoice.getDetailSeats().stream()
                    .findFirst()
                    .map(DetailSeat::getSchedule)
                    .orElse(null);
            if (schedule == null || schedule.getRoom() == null || schedule.getRoom().getTheater() == null) continue;

            Theater theater = schedule.getRoom().getTheater();
            Integer customerId = customer.getId();
            Integer theaterId = theater.getTheaterID();

            // Nếu đã có phản hồi thì bỏ qua
            if (feedbackServiceRepository.existsByCustomer_IdAndTheaterId(customerId, theaterId)) {
                continue;
            }

            String toEmail = customer.getAccount().getEmail();
            String customerName = customer.getFullName();
            String theaterName = theater.getTheaterName();
            Integer invoiceId = invoice.getInvoiceID();

            // Gửi email phản hồi
            emailService.sendFeedbackRequestEmail(toEmail, customerName, theaterName, invoiceId);
        }
    }

}


