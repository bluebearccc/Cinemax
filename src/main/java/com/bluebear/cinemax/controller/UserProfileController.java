package com.bluebear.cinemax.controller;
import  com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.enumtype.FeedbackStatus;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.FeedbackServiceRepository;
import com.bluebear.cinemax.service.CustomerCareService;
import com.bluebear.cinemax.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private FeedbackServiceRepository feedbackServiceRepository;
    @Autowired
    private CustomerRepository  customerRepository;

    @GetMapping("/user/profile")
    public String showUserProfile(@RequestParam("customerId") Integer customerId, Model model) {
        CustomerDTO customer = userProfileService.getCustomerById(customerId);
        if (customer == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "error/404";
        }

        AccountDTO account = userProfileService.getAccountById(customer.getAccountID().longValue());
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
    @GetMapping("/user/profile/edit")
    public String showEditProfileForm(@RequestParam("customerId") Integer customerId, Model model) {
        CustomerDTO customer = userProfileService.getCustomerById(customerId);
        if (customer == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "error/404";
        }

        AccountDTO account = userProfileService.getAccountById(customer.getAccountID().longValue());


        model.addAttribute("customer", customer);
        model.addAttribute("account", account);

        return "common/edit_profile"; // Tên file .html bạn đã tạo
    }
    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam("customerId") Integer customerId,
                                @RequestParam("accountId") Long accountId,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("email") String email,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {

        CustomerDTO customer = userProfileService.getCustomerById(customerId);
        AccountDTO account = userProfileService.getAccountById(accountId);
//        Theater theater =userProfileService.getTheaterById(theaterId);

        if (customer == null || account == null) {
            model.addAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "error/404";
        }

        // Kiểm tra email đã tồn tại chưa (nếu có đổi)
        if (!account.getEmail().equals(email) && userProfileService.emailExists(email)) {
            model.addAttribute("errorMessage", "Email đã được sử dụng.");
            model.addAttribute("customer", customer); // Thêm dòng này
            model.addAttribute("account", account);
//            model.addAttribute("theater", theater);
            return "common/edit_profile";
        }


        // Cập nhật thông tin
        customer.setFullName(fullName);
        customer.setPhone(phone);
        account.setEmail(email);

        // Nếu người dùng nhập mật khẩu mới
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
                model.addAttribute("customer", customer); // Thêm dòng này
                model.addAttribute("account", account);
//                model.addAttribute("theater", theater);
                return "common/edit_profile";
            }


            // Nên mã hóa mật khẩu
            account.setPassword(newPassword);

        }

        userProfileService.saveCustomer(customer);
        userProfileService.saveAccount(account);

        return "redirect:/user/profile?customerId=" + customerId;
    }

    @PostMapping("/feedback/submit")
    public String submitServiceFeedback(@RequestParam("customerId") Integer customerId,
                                        @RequestParam("theaterId") Integer theaterId,
                                        @RequestParam("content") String content,
                                        @RequestParam("serviceRate") Integer serviceRate,
                                        RedirectAttributes redirectAttributes) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        FeedbackService feedback = new FeedbackService();
        feedback.setCustomer(customer);
        feedback.setTheaterId(theaterId);
        feedback.setContent(content);
        feedback.setServiceRate(serviceRate);
        feedback.setCreatedDate(LocalDateTime.now());
        if (serviceRate < 4) {
            feedback.setStatus(FeedbackStatus.Not_Suported);
        } else {
            feedback.setStatus(FeedbackStatus.Suported);
        }

        feedbackServiceRepository.save(feedback); // Inject repository

        redirectAttributes.addFlashAttribute("successMessage", "Gửi đánh giá thành công!");
        return "redirect:/user/profile?customerId=" + customerId;
    }


}
