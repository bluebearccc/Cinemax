package com.bluebear.cinemax.service.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateEngine templateEngine;


    public void sendMailTime(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.addInline("logo", new ClassPathResource("static/customer-static/images/logo/logo.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mailSender.send(message);
    }

    public String buildEmailContent(String username, String link) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("link", link);

        return templateEngine.process("common/email-confirmation", context);
    }

    public String builEmailContentForResetPassword(List<Integer> otp) {
        Context context = new Context();
        context.setVariable("otp", otp);

        return templateEngine.process("common/otp-confirmation", context);
    }
}
