package com.bluebear.cinemax.controller.homepage;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.dto.Movie.InvoiceDetailDTO;
import com.bluebear.cinemax.entity.*;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.repository.ServiceFeedbackRepository;
import com.bluebear.cinemax.repository.InvoiceRepository;
import com.bluebear.cinemax.service.email.EmailService;
import com.bluebear.cinemax.service.UserProfileService;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.theater.TheaterService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserProfileController {
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private ServiceFeedbackRepository serviceFeedbackRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserProfileService userProfileService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private CustomerRepository customerRepository;

    private List<GenreDTO> genres;
    private Page<TheaterDTO> theaters;

    @GetMapping("/user/profile")
    public String showUserProfile(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "4") int size,
                                  Model model, HttpSession session) {
        CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
        if (customerDTO == null) {
            model.addAttribute("errorMessage", "User not found.");
            return "error/404";
        }

        AccountDTO account = userProfileService.getAccountById(customerDTO.getAccountID());
        List<InvoiceDTO> watchedInvoices = userProfileService.getBookedInvoicesByCustomer(customerDTO);
        boolean hasWatched = userProfileService.hasWatchedMovies(customerDTO);
        List<WatchedMovieDTO> watchedMovies = userProfileService.getWatchedMovies(customerDTO);
        // Search
        if (keyword != null && !keyword.isBlank()) {
            watchedMovies = watchedMovies.stream()
                    .filter(movie -> movie.getMovie().getMovieName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            model.addAttribute("keyword", keyword);
        }
        // 🔢 Pagination
        int totalItems = watchedMovies.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int fromIndex = Math.max((page - 1) * size, 0);
        int toIndex = Math.min(fromIndex + size, totalItems);
        List<WatchedMovieDTO> pageList = watchedMovies.subList(fromIndex, toIndex);
        genres = genreService.getAllGenres();
        theaters = theaterService.getAllTheaters();
        model.addAttribute("theaters", theaters);
        model.addAttribute("genres", genres);
        model.addAttribute("account", account);
        model.addAttribute("customer", customerDTO);
        model.addAttribute("watchedInvoices", watchedInvoices);
        model.addAttribute("hasWatched", hasWatched);
        model.addAttribute("watchedMovies", pageList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);

        return "common/profile";
    }

    @PostMapping("/user/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("email") String email,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                @RequestParam("otp") String otpInput,
                                Model model, HttpSession session) {

        CustomerDTO customerDTO = (CustomerDTO) session.getAttribute("customer");
        AccountDTO account = userProfileService.getAccountById(customerDTO.getAccountID());

        if (customerDTO == null || account == null) {
            model.addAttribute("errorMessage", "User not found.");
            return "error/404";
        }

        genres = genreService.getAllGenres();
        theaters = theaterService.getAllTheaters();
        // OTP
        String sessionOtp = (String) session.getAttribute("otp");
        LocalDateTime otpTime = (LocalDateTime) session.getAttribute("otpTime");
        if (!fullName.matches("^[^\\d!@#$%^&*()_+={}\\[\\]:;\"'<>,.?/\\\\|`~]+$") || fullName.trim().isEmpty()) {

            model.addAttribute("theaters", theaters);
            model.addAttribute("genres", genres);
            model.addAttribute("errorMessage", "Invalid full name: cannot contain numbers or special characters.");
            populateUserProfileModel(model, customerDTO, account);
            model.addAttribute("inputFullName", fullName);
            model.addAttribute("inputPhone", phone);
            model.addAttribute("inputEmail", email);
            return "common/profile";
        }

        if (sessionOtp == null || otpTime == null || !sessionOtp.equals(otpInput) || otpTime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMessage", "Invalid or expired OTP.");

            populateUserProfileModel(model, customerDTO, account);
            model.addAttribute("inputFullName", fullName);
            model.addAttribute("inputPhone", phone);
            model.addAttribute("inputEmail", email);
            model.addAttribute("theaters", theaters);
            model.addAttribute("genres", genres);
            return "common/profile";
        }

        // Check if email already exists (if changed)
        if (!account.getEmail().equals(email) && userProfileService.emailExists(email)) {
            model.addAttribute("errorMessage", "This email is already in use.");
            populateUserProfileModel(model, customerDTO, account);
            model.addAttribute("theaters", theaters);
            model.addAttribute("genres", genres);
            return "common/profile";
        }

        // Update information
        customerDTO.setFullName(fullName);
        customerDTO.setPhone(phone);
        account.setEmail(email);

        // If user enters a new password
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Password confirmation does not match.");
                populateUserProfileModel(model, customerDTO, account);
                model.addAttribute("theaters", theaters);
                model.addAttribute("genres", genres);
                return "common/profile";
            }

            // Password should be encrypted
            account.setPassword(newPassword);
        }

        userProfileService.saveCustomer(customerDTO);
        userProfileService.saveAccount(account);

        return "redirect:/user/profile?customerId=" + customerDTO;
    }

    @PostMapping("/feedback/submit")
    public String submitServiceFeedback(@ModelAttribute ServiceFeedbackDTO feedbackDTO,
                                        RedirectAttributes redirectAttributes) {
        userProfileService.submitFeedback(feedbackDTO); // Inject repository

        redirectAttributes.addFlashAttribute("successMessage", "Feedback submitted successfully!");
        return "redirect:/user/profile?customerId=" + feedbackDTO.getCustomerId();
    }

    @GetMapping("/invoice/{invoiceId}/detail")
    @ResponseBody
    public ResponseEntity<InvoiceDetailDTO> getInvoiceDetail(@PathVariable("invoiceId") Integer invoiceId) {
        InvoiceDetailDTO dto = userProfileService.getInvoiceDetailById(invoiceId);
        return ResponseEntity.ok(dto);
    }

    // OTP
    @PostMapping("/user/send-otp")
    @ResponseBody
    public ResponseEntity<String> sendOtp(@RequestParam("email") String email, HttpSession session) {
        Optional<Account> account = userProfileService.getAccountByEmail(email);
        if (account == null) return ResponseEntity.badRequest().body("Email does not exist!");

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        session.setAttribute("otp", otp);
        session.setAttribute("otpTime", LocalDateTime.now());

        emailService.sendOtpEmail(email, otp); // Send email via service
        return ResponseEntity.ok("An OTP has been sent to your email.");
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

        return "chat/feedback-form";
    }

    // Handle feedback submitted by the user
    @PostMapping("/submit")
    public String submitFeedback(@ModelAttribute ServiceFeedbackDTO feedbackDTO, Model model) {
        userProfileService.submitFeedback(feedbackDTO);
        genres = genreService.getAllGenres();
        theaters = theaterService.getAllTheaters();
        model.addAttribute("message", "Thank you for your feedback!");
        model.addAttribute("theaters", theaters);
        model.addAttribute("genres", genres);
        return "common/feedback-thankyou";
    }
}