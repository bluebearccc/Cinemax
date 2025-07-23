package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.*;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.security.AccountDetailService;
import com.bluebear.cinemax.service.account.AccountServiceImpl;
import com.bluebear.cinemax.service.customer.CustomerServiceImpl;
import com.bluebear.cinemax.service.email.EmailServiceImpl;
import com.bluebear.cinemax.service.employee.EmployeeServiceImpl;
import com.bluebear.cinemax.service.forgotpassword.ForgotPasswordServiceImpl;
import com.bluebear.cinemax.service.genre.GenreService;
import com.bluebear.cinemax.service.theater.TheaterService;
import com.bluebear.cinemax.service.verifytoken.VerifyTokenServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping
public class AuthenticationController {

    @Autowired
    private AccountServiceImpl accountService;
    @Autowired
    private CustomerServiceImpl customerService;
    @Autowired
    private EmployeeServiceImpl employeeService;
    @Autowired
    private ForgotPasswordServiceImpl forgotPasswordService;
    @Autowired
    private EmailServiceImpl emailService;
    @Autowired
    private VerifyTokenServiceImpl verifyTokenService;
    @Autowired
    private GenreService genreService;
    @Autowired
    private TheaterService theaterService;
    @Autowired
    private AccountDetailService accountDetailService;

    private List<GenreDTO> genres;
    private Page<TheaterDTO> theaters;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        genres = genreService.getAllGenres();
        theaters = theaterService.getAllTheaters();
    }

    @GetMapping("/login")
    public String login(Model model, @CookieValue(name = "LOGIN_ERROR", required = false) String loginError, HttpServletResponse response) {
        model.addAttribute("genres", genres);
        model.addAttribute("theaters", theaters);
        if (loginError != null) {
            try {
                String decodedError = URLDecoder.decode(loginError, StandardCharsets.UTF_8);
                model.addAttribute("error", decodedError);
            } catch (Exception e) {
                model.addAttribute("error", "Unknown error");
            }

            Cookie cookie = new Cookie("LOGIN_ERROR", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "common/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("genres", genres);
        model.addAttribute("theaters", theaters);
        return "common/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("fullName") String fullName, Model model, HttpSession session) {
        AccountDTO account = accountService.findAccountByEmail(email.trim());
        model.addAttribute("genres", genres);
        model.addAttribute("theaters", theaters);
        if (account != null) {
            model.addAttribute("error", "Existed User");
            return "common/register";
        } else {
            if (verifyTokenService.getTokenByEmail(email.trim()) == null) {
                String token = UUID.randomUUID().toString();
                VerifyTokenDTO verifyTokenDTO = VerifyTokenDTO.builder().email(email.trim()).token(token).expiresAt(new Date(System.currentTimeMillis() + 5 * 60 * 1000)).fullName(fullName.trim()).password(password.trim()).build();
                verifyTokenService.create(verifyTokenDTO);
                String verifyLink = baseUrl + "/verifytoken?token=" + token;
                String subject = "Email Verification";
                String content = emailService.buildEmailContent(fullName, verifyLink);
                emailService.sendMailTime(email, subject, content);
                model.addAttribute("inform", "Verification email sent. Please check your inbox.");
            } else {
                model.addAttribute("error", "Existed Token");
            }

            return "common/register";

        }
    }

    @GetMapping("/verifytoken")
    public String verifyToken(@RequestParam("token") String token, HttpSession session, Model model) {
        VerifyTokenDTO verifyTokenDTO = verifyTokenService.findByToken(token);
        if (verifyTokenDTO == null) {
            model.addAttribute("error", "Invalid token");
            return "common/register";
        } else if (verifyTokenDTO.getExpiresAt().before(new Date(System.currentTimeMillis()))) {
            verifyTokenService.deleteTokenByEmail(verifyTokenDTO.getEmail());
            model.addAttribute("error", "Expired token");
            return "common/register";
        } else {
            AccountDTO accountDTO = AccountDTO.builder().email(verifyTokenDTO.getEmail()).password(verifyTokenDTO.getPassword()).role(Role.Customer).status(Account_Status.Active).build();
            AccountDTO accountDTO1 = accountService.save(accountDTO);
            session.setAttribute("account", accountDTO1);
            CustomerDTO customerDTO = CustomerDTO.builder().accountID(accountDTO1.getId()).fullName(verifyTokenDTO.getFullName()).build();
            CustomerDTO customerDTO1 = customerService.save(customerDTO);
            session.setAttribute("customer", customerDTO1);
            verifyTokenService.deleteTokenByEmail(verifyTokenDTO.getEmail());
            UserDetails userDetails = accountDetailService.loadUserByUsername(accountDTO.getEmail());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/";
        }
    }

    @GetMapping("/forgotpassword")
    public String forgotpassword(Model model) {
        return "common/verify-email";
    }

    @GetMapping("/resendotp")
    public String resendotp(@RequestParam("email") String email, HttpSession session, Model model) {
        AccountDTO account = (AccountDTO) session.getAttribute("account");
        ForgotPasswordDTO forgotPasswordDTO = forgotPasswordService.findForgotPasswordByAccountId(account.getId());
        if (forgotPasswordDTO != null) {
            forgotPasswordService.deleteForgotPassword(forgotPasswordDTO.getId());
        }
        List<Integer> otpDigits = generateOtpDigits();
        int otp = convertDigitsToInteger(otpDigits);
        forgotPasswordDTO = ForgotPasswordDTO.builder().accountId(account.getId()).otp(otp).expiryDate(new Date(System.currentTimeMillis() + 5 * 60 * 1000)).build();
        forgotPasswordService.createForgotPassword(forgotPasswordDTO);
        String content = emailService.builEmailContentForResetPassword(otpDigits);
        emailService.sendMailTime(email, "Reset Password", content);
        return "common/verify-otp";
    }

    @GetMapping("/verifyemail")
    public String verifyemail(@RequestParam("email") String email, RedirectAttributes redirectAttributes, Model model, HttpSession session) {
        AccountDTO account = accountService.findAccountByEmail(email);
        if (account != null && account.getStatus() == Account_Status.Active) {
            session.setAttribute("account", account);
            model.addAttribute("email", email);
            ForgotPasswordDTO forgotPasswordDTO = forgotPasswordService.findForgotPasswordByAccountId(account.getId());
            if (forgotPasswordDTO != null) {
                if (forgotPasswordDTO.getExpiryDate().before(new Date(System.currentTimeMillis()))) {
                    forgotPasswordService.deleteForgotPassword(forgotPasswordDTO.getId());
                } else {
                    return "common/verify-otp";
                }
            }
            List<Integer> otpDigits = generateOtpDigits();
            int otp = convertDigitsToInteger(otpDigits);
            forgotPasswordDTO = ForgotPasswordDTO.builder().accountId(account.getId()).otp(otp).expiryDate(new Date(System.currentTimeMillis() + 5 * 60 * 1000)).build();
            forgotPasswordService.createForgotPassword(forgotPasswordDTO);
            String content = emailService.builEmailContentForResetPassword(otpDigits);
            emailService.sendMailTime(email, "Reset Password", content);
            return "common/verify-otp";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email");
            return "redirect:/forgotpassword";
        }
    }

    @PostMapping("/otp")
    public String otp(@RequestParam("email") String email, @RequestParam("otp") String otp, Model model, HttpSession session) {
        Integer otpInt = null;

        try {
            otpInt = Integer.parseInt(otp);
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid otp");
            return "common/verify-otp";
        }

        AccountDTO account = (AccountDTO) session.getAttribute("account");
        if (account != null) {
            ForgotPasswordDTO forgotPasswordDTO = forgotPasswordService.findForgotPasswordByAccountId(account.getId());
            if (forgotPasswordDTO != null && !otpInt.equals(forgotPasswordDTO.getOtp())) {
                model.addAttribute("error", "Invalid otp");
                return "common/verify-otp";
            } else if (forgotPasswordDTO.getExpiryDate().before(new Date(System.currentTimeMillis()))) {
                forgotPasswordService.deleteForgotPassword(forgotPasswordDTO.getId());
                model.addAttribute("error", "Expired otp");
                return "common/verify-otp";
            }
            forgotPasswordService.deleteForgotPassword(forgotPasswordDTO.getId());
            return "redirect:/newpass";
        } else {
            return "redirect:/forgotpassword";
        }

    }

    @GetMapping("/newpass")
    public String newpass(Model model) {
        return "common/reset-password";
    }

    @PostMapping("/updatepassword")
    public String updatePassword(@RequestParam("password") String password, HttpSession session, Model model) {
        AccountDTO account = (AccountDTO) session.getAttribute("account");
        if (account != null) {
            account.setPassword(password);
            AccountDTO newAcc = accountService.save(account);
            session.setAttribute("account", newAcc);
            CustomerDTO customerDTO = customerService.getUserByAccountID(account.getId());
            if (customerDTO != null) {
                session.setAttribute("customer", customerDTO);
            } else {
                session.setAttribute("employee", employeeService.findByAccountId(account.getId()));
            }
            return switch (account.getRole()) {
                case Admin -> "redirect:/admin/dashboard";
                case Customer -> "redirect:";
                case Staff -> "redirect:/staff/home";
                case Cashier -> "redirect:/cashier/home";
                case Customer_Officer -> "redirect:/officer/home";
                default -> "redirect:/newpass";
            };
        }
        model.addAttribute("error", "Something went wrong");
        return "redirect:/newpass";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:";
    }

    public List<Integer> generateOtpDigits() {
        Random rand = new Random();
        List<Integer> otpDigits = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            otpDigits.add(rand.nextInt(10));
        }
        return otpDigits;
    }

    public int convertDigitsToInteger(List<Integer> otpDigits) {
        StringBuilder sb = new StringBuilder();
        for (Integer digit : otpDigits) {
            sb.append(digit);
        }
        return Integer.parseInt(sb.toString());
    }

}
