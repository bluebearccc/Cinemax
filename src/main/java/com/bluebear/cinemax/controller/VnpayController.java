package com.bluebear.cinemax.controller;
import com.bluebear.cinemax.service.EmailService;
import com.bluebear.cinemax.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class VnpayController {

    private final VnpayService vnpayService;
    private final EmailService emailService;
    @GetMapping("/vnpay_return")
    public String handleVnpayReturn(HttpServletRequest request, Model model) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        String txnRef = request.getParameter("vnp_TxnRef");
        String amount = request.getParameter("vnp_Amount");
        String bankCode = request.getParameter("vnp_BankCode");

        if (responseCode == null || txnRef == null || amount == null) {
            throw new IllegalArgumentException("Missing required parameters from VNPAY response.");
        }

        model.addAttribute("responseCode", responseCode);
        model.addAttribute("transactionStatus", transactionStatus);
        model.addAttribute("invoiceId", txnRef);
        model.addAttribute("amount", amount);
        model.addAttribute("bankCode", bankCode);

        if ("00".equals(responseCode)) {
            model.addAttribute("message", "Thanh toán thành công!");

            // ✅ Gửi email demo khi thanh toán thành công
            try {
                String email = "nguyentavan188@gmail.com"; // ← thay bằng email của bạn
                String subject = "Xác nhận đặt vé thành công - Mã hóa đơn #" + txnRef;
                String content = "<h3>🎟️ Đặt vé thành công!</h3>"
                        + "<p>Mã hóa đơn: <strong>" + txnRef + "</strong></p>"
                        + "<p>Số tiền: <strong>" + (Long.parseLong(amount) / 100) + " VND</strong></p>"
                        + "<p>Ngân hàng: <strong>" + bankCode + "</strong></p>"
                        + "<p>Cảm ơn bạn đã đặt vé tại Cinemax!</p>";

                emailService.sendTicketCode(email, subject, content);
                System.out.println("✅ Email đã gửi đến: " + email);
            } catch (Exception e) {
                System.err.println("❌ Gửi email thất bại: " + e.getMessage());
            }

        } else {
            model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
        }

        return "common/vnpay_return";
    }

}
