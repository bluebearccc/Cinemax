package com.bluebear.cinemax.controller.customer;

import  com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.repository.FeedbackServiceRepository;
import com.bluebear.cinemax.service.EmailService;
import com.bluebear.cinemax.service.UserProfileService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class UserProfileController {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private FeedbackServiceRepository serviceFeedbackRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private CustomerRepository  customerRepository;

    @GetMapping("/user/profile")
    public String showUserProfile(@RequestParam("customerId") Integer customerId, Model model) {
        CustomerDTO customer = userProfileService.getCustomerById(customerId);
        if (customer == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "error/404";
        }

        AccountDTO account = userProfileService.getAccountById(customer.getAccountID());
        List<InvoiceDTO> watchedInvoices = userProfileService.getBookedInvoicesByCustomer(customer);
        boolean hasWatched = userProfileService.hasWatchedMovies(customer);
        List<WatchedMovieDTO> watchedMovies = userProfileService.getWatchedMovies(customer);

        model.addAttribute("account", account);
        model.addAttribute("customer", customer);
        model.addAttribute("watchedInvoices", watchedInvoices);
        model.addAttribute("hasWatched", hasWatched);
        model.addAttribute("watchedMovies", watchedMovies);

        return "common/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam("customerId") Integer customerId,
                                @RequestParam("accountId") Integer accountId,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("email") String email,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam("otp") String otpInput,
                                Model model,HttpSession session) {

        CustomerDTO customer = userProfileService.getCustomerById(customerId);
        AccountDTO account = userProfileService.getAccountById(accountId);
//        Theater theater =userProfileService.getTheaterById(theaterId);

        if (customer == null || account == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "error/404";
        }
        //otp
        String sessionOtp = (String) session.getAttribute("otp");
        LocalDateTime otpTime = (LocalDateTime) session.getAttribute("otpTime");

        if (sessionOtp == null || otpTime == null || !sessionOtp.equals(otpInput) || otpTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage", "OTP không hợp lệ hoặc đã hết hạn.");

            populateUserProfileModel(model, customer, account);
            model.addAttribute("inputFullName", fullName);
            model.addAttribute("inputPhone", phone);
            model.addAttribute("inputEmail", email);

            return "common/profile";
        }




        // Kiểm tra email đã tồn tại chưa (nếu có đổi)
        if (!account.getEmail().equals(email) && userProfileService.emailExists(email)) {
            model.addAttribute("errorMessage", "Email đã được sử dụng.");
            populateUserProfileModel(model, customer, account);
            return "common/profile";
        }



        // Cập nhật thông tin
        customer.setFullName(fullName);
        customer.setPhone(phone);
        account.setEmail(email);

        // Nếu người dùng nhập mật khẩu mới
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
                populateUserProfileModel(model, customer, account);
                return "common/profile";
            }



            // Nên mã hóa mật khẩu
            account.setPassword(newPassword);

        }

        userProfileService.saveCustomer(customer);
        userProfileService.saveAccount(account);

        return "redirect:/user/profile?customerId=" + customerId;
    }

    @PostMapping("/feedback/submit")
    public String submitServiceFeedback(@ModelAttribute FeedbackServiceDTO feedbackDTO,
                                        RedirectAttributes redirectAttributes) {
        userProfileService.submitFeedback(feedbackDTO); // Inject repository

        redirectAttributes.addFlashAttribute("successMessage", "Gửi đánh giá thành công!");
        return "redirect:/user/profile?customerId=" + feedbackDTO.getCustomerId();
    }
    @PostMapping("/invoice/{invoiceId}/detail")
    @ResponseBody
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetail(@PathVariable("invoiceId") Integer invoiceId) {
        InvoiceDetailDTO dto = userProfileService.getInvoiceDetailById(invoiceId);
        return ResponseEntity.ok(dto);
    }
    //otp
    @PostMapping("/user/send-otp")
    @ResponseBody
    public ResponseEntity<String> sendOtp(@RequestParam("email") String email, HttpSession session) {
        Optional<Account> account = userProfileService.getAccountByEmail(email);
        if (account == null) return ResponseEntity.badRequest().body("Email không tồn tại!");

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpTime", LocalDateTime.now());

        emailService.sendOtpEmail(email, otp); // Gửi mail qua service
        return ResponseEntity.ok("OTP đã được gửi về email.");
    }
    private void populateUserProfileModel(Model model, CustomerDTO customer, AccountDTO account) {
        model.addAttribute("customer", customer);
        model.addAttribute("account", account);
        model.addAttribute("watchedInvoices", userProfileService.getBookedInvoicesByCustomer(customer));
        model.addAttribute("hasWatched", userProfileService.hasWatchedMovies(customer));
        model.addAttribute("watchedMovies", userProfileService.getWatchedMovies(customer));
    }

    @GetMapping("/form")
    public String showFeedbackForm(@RequestParam("invoiceId") Integer invoiceId, Model model) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        if (invoice == null) return "error/404";

        model.addAttribute("invoiceId", invoiceId);
        model.addAttribute("customerName", invoice.getCustomer().getFullName());
        model.addAttribute("theaterName", invoice.getDetailSeats().get(0).getSchedule().getRoom().getTheater().getTheaterName());

        return "common/feedback-form";
    }

    // Xử lý phản hồi được gửi từ người dùng
    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute FeedbackServiceDTO feedbackDTO, Model model) {
        userProfileService.submitFeedback(feedbackDTO);
        model.addAttribute("message", "Cảm ơn bạn đã gửi phản hồi!");
        return "common/feedback-thankyou";
    }



}

