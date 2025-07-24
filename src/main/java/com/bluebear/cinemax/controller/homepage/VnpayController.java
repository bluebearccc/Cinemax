package com.bluebear.cinemax.controller.homepage;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.MovieRepository;
import com.bluebear.cinemax.repository.RoomRepository;
import com.bluebear.cinemax.service.VnpayService;
import com.bluebear.cinemax.service.bookingSF.BookingServiceSF;
import com.bluebear.cinemax.service.email.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class VnpayController {
    @Autowired
    private  InvoiceRepository invoiceRepo;
    @Autowired
    private  VnpayService vnpayService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingServiceSF  bookingService;


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

            try {
                int invoiceId = Integer.parseInt(txnRef.split("_")[0].replace("INV", ""));

                InvoiceDTO invoiceDTO = vnpayService.getInvoiceDTOById(invoiceId);
                String email = invoiceDTO.getCustomer().getAccount().getEmail();

                String subject = "🎟️ Vé xem phim thành công - Hóa đơn #" + txnRef;
                vnpayService.confirmInvoiceAfterPayment(invoiceId);


                // Lấy lịch chiếu từ ghế đầu tiên
                DetailSeatDTO firstSeat = invoiceDTO.getDetailSeats().getFirst();
                Integer scheduleId = firstSeat.getScheduleID();

                // Gọi service (hoặc tự mở rộng getInvoiceDTOById để trả luôn dữ liệu này)
                ScheduleDTO schedule = vnpayService.getScheduleDTO(scheduleId);

                // Lấy danh sách vị trí ghế
                List<String> seatPositions = invoiceDTO.getDetailSeats().stream()
                        .map(ds -> vnpayService.getSeatPosition(ds.getSeatID()))
                        .collect(Collectors.toList());

                String seatString = String.join(", ", seatPositions);

                Map<String, Object> emailData = Map.of(
                        "invoiceId", txnRef,
                        "amount", Long.parseLong(amount) / 100,
                        "bankCode", bankCode,
                        "movieName", schedule.getMovieName(),
                        "room", schedule.getRoomName(),
                        "seats", seatString,
                        "showtime", schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))
                );

                emailService.sendTicketHtmlTemplate(email, subject, emailData);
                System.out.println("✅ Email gửi thành công tới: " + email);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            }

        } else {
            model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);

            try {
                int invoiceId = Integer.parseInt(txnRef);
                // Cập nhật trạng thái hóa đơn
                bookingService.cancelInvoice(invoiceId);
                System.out.println("🚫 Cập nhật trạng thái CANCELLED cho hóa đơn #" + invoiceId);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi cập nhật trạng thái cancelled: " + e.getMessage());
            }
        }


        return "common/vnpay_return";
    }

}
