package com.bluebear.cinemax.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

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
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP để đổi mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã có hiệu lực trong 5 phút.");

        mailSender.send(message);
    }
    public void sendFeedbackRequestEmail(String toEmail, String customerName, String theaterName, Integer invoiceId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("theaterName", theaterName);
            context.setVariable("feedbackLink", "http://yourdomain.com/feedback/form?invoiceId=" + invoiceId);

            String htmlContent = templateEngine.process("common/feedback-request-template", context);

            helper.setTo(toEmail);
            helper.setSubject("Phản hồi trải nghiệm tại rạp " + theaterName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email phản hồi thất bại", e);
        }
    }



}
