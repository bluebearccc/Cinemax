package com.bluebear.cinemax.service.email;

import java.util.Map;

public interface EmailService {
    void sendTicketHtmlTemplate(String toEmail, String subject, Map<String, Object> variables);
    void sendOtpEmail(String toEmail, String otp);
    void sendFeedbackRequestEmail(String toEmail, String customerName, String theaterName, Integer invoiceId);
    void sendNotifyScheduleEmail(String toEmail, String subject, String body);

}
