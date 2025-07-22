package com.bluebear.cinemax.service.bookingSF;


import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.DetailSeat_Status;
import com.bluebear.cinemax.enumtype.InvoiceStatus;
import com.bluebear.cinemax.repository.*;
import com.bluebear.cinemax.service.email.EmailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PendingBookingService {
    @Autowired
    private FeedbackServiceRepository feedbackServiceRepository;
    @Autowired
    private InvoiceRepository invoiceRepo;
    @Autowired
    private DetailFDRepository detailFDRepo;
    @Autowired
    private TheaterStockRepository theaterStockRepo;
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
    @Scheduled(fixedRate = 60000) // mỗi phút
    @Transactional
    public void cancelExpiredInvoices() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(15);
        List<Invoice> expired = invoiceRepo.findByStatusAndBookingDateBefore(InvoiceStatus.Unpaid, expiredTime);
        for (Invoice invoice : expired) {
            cancelInvoice(invoice.getInvoiceID());
            log.info("🕒 Đã tự động hủy hóa đơn quá hạn: {}", invoice.getInvoiceID());
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

    @Transactional
    public void cancelInvoice(Integer invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn #" + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.Unpaid) {
            // ➕ Trả lại số lượng combo đã giữ (Detail_FD)
            List<Detail_FD> detailFDs = detailFDRepo.findByInvoiceInvoiceID(invoiceId);
            for (Detail_FD fd : detailFDs) {
                TheaterStock stock = fd.getTheaterStock();
                stock.setQuantity(stock.getQuantity() + fd.getQuantity());
                theaterStockRepo.save(stock); // cập nhật lại số lượng
            }
            detailFDRepo.deleteAll(detailFDs);

            // ❌ Xóa tất cả các ghế đã giữ (DetailSeat)
            List<DetailSeat> detailSeats = detailSeatRepo.findByInvoiceInvoiceID(invoiceId);
            detailSeatRepo.deleteAll(detailSeats);

            // 🔁 Cập nhật trạng thái hóa đơn thành CANCELED
            invoice.setStatus(InvoiceStatus.Cancelled);
            invoiceRepo.save(invoice);

            log.info("🗑️ Đã huỷ hóa đơn #{} và giải phóng tài nguyên.", invoiceId);
        } else {
            log.warn("⚠️ Không thể huỷ hóa đơn #{} vì không ở trạng thái UNPAID.", invoiceId);
        }
    }

}
