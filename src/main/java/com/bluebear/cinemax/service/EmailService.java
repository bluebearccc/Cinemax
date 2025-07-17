package com.bluebear.cinemax.service;
import com.bluebear.cinemax.entity.DetailSeat;
import com.bluebear.cinemax.repository.InvoiceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendTicketCode(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }
    public void sendTicketHtmlTemplate(String toEmail, String subject, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("common/ticket-template", context);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại", e);
        }
    }
    @Autowired
    private InvoiceRepository invoiceRepository; // Giả sử bạn có repository này

    public void sendSeatCancellationNotice(List<DetailSeat> bookings) {
        for (DetailSeat booking : bookings) {

            // === 1. LẤY EMAIL CỦA KHÁCH HÀNG ===
            // Dùng try-catch để xử lý an toàn nếu một trong các đối tượng (invoice, customer, account) bị null
            String customerEmail;
            try {
                // Truy cập trực tiếp qua các mối quan hệ đã được định nghĩa trong Entity
                customerEmail = booking.getInvoice().getCustomer().getAccount().getEmail();
            } catch (NullPointerException e) {
                // Ghi log lỗi và bỏ qua booking này nếu không tìm thấy chuỗi thông tin
                System.err.println("Could not retrieve customer email for booking with seat: " + booking.getSeat().getPosition() + ". Skipping.");
                continue; // Bỏ qua và xử lý booking tiếp theo trong vòng lặp
            }

            // === 2. CHUẨN BỊ NỘI DUNG VÀ GỬI EMAIL ===
            // Chỉ thực hiện nếu đã lấy được email thành công
            if (customerEmail != null && !customerEmail.isEmpty()) {
                String movieName = booking.getSchedule().getMovie().getMovieName();
                String seatPosition = booking.getSeat().getPosition();
                // Định dạng lại thời gian cho dễ đọc hơn
                String showTime = booking.getSchedule().getStartTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm 'on' dd-MM-yyyy"));

                String subject = "Important Update Regarding Your Booking at Cinemax";
                String body = String.format(
                        "Dear Customer,\n\n" +
                                "We regret to inform you that due to a technical issue, seat %s for the movie '%s' at %s is no longer available.\n\n" +
                                "Your booking for this seat has been cancelled. Please contact our customer support to arrange for a full refund or to rebook a different seat.\n\n" +
                                "We sincerely apologize for this inconvenience.\n\n" +
                                "Best regards,\nThe Cinemax Team",
                        seatPosition, movieName, showTime
                );

                // Gọi hàm send thực tế để gửi mail
                send(customerEmail, subject, body);
            }
        }
    }
    private void send(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("hausd12@gmail.com");

            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            System.out.println("Email sent successfully to " + toEmail);

        } catch (MailException e) {
            System.err.println("Error sending email to " + toEmail + ": " + e.getMessage());
        }
    }

}
