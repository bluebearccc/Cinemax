package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.ForgotPasswordDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.repository.ForgotPasswordRepository;
import com.bluebear.cinemax.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping
public class AuthenticationController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String login() {
        return "common/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model model, HttpSession session) {
        AccountDTO account = accountService.findAccountByEmail(email);
        if (account == null || !account.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid email or password");
            return "common/login";
        } else if (account.getStatus() == Account_Status.Banned) {
            model.addAttribute("error", "opps! this account is banned");
            return "common/login";
        } else {
            session.setAttribute("account", account);
            CustomerDTO customerDTO = customerService.getUserByAccountID(account.getId());
            if (customerDTO != null) {
                session.setAttribute("customer", customerDTO);
            } else {
                session.setAttribute("employee", employeeService.findByAccountId(account.getId()));
            }

            return switch (account.getRole()) {
                case Admin -> "redirect:/admin/dashboard";
                case Customer -> "redirect:/home";
                case Staff -> "redirect:/staff/home";
                case Cashier -> "redirect:/cashier/home";
                case Customer_Officer -> "redirect:/officer/home";
                default -> {
                    model.addAttribute("error", "Unknown role");
                    yield "common/login";
                }
            };
        }
    }

    @GetMapping("/register")
    public String register() {
        return "common/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("email") String email, @RequestParam("password") String password, @RequestParam("fullName") String fullName, Model model, HttpSession session) {
        AccountDTO account = accountService.findAccountByEmail(email);
        if (account != null) {
            model.addAttribute("error", "Existed User");
            return "common/register";
        } else {
            AccountDTO accountDTO = AccountDTO.builder().email(email.trim()).password(password.trim()).role(Role.Customer).status(Account_Status.Active).build();
            AccountDTO accountDTO1 = accountService.save(accountDTO);
            session.setAttribute("account", accountDTO);
            CustomerDTO customerDTO = CustomerDTO.builder().fullName(fullName.trim()).accountID(accountDTO1.getId()).build();
            CustomerDTO customerDTO1 = customerService.save(customerDTO);
            session.setAttribute("customer", customerDTO1);
            return "redirect:/home";
        }
    }

    @GetMapping("/forgotpassword")
    public String forgotpassword() {
        return "common/verify-email";
    }

    @GetMapping("/resendotp")
    public String resendotp(@RequestParam("email") String email, HttpSession session) {
        AccountDTO account = (AccountDTO) session.getAttribute("account");
        ForgotPasswordDTO forgotPasswordDTO = forgotPasswordService.findForgotPasswordByAccountId(account.getId());
        if (forgotPasswordDTO != null) {
            forgotPasswordService.deleteForgotPassword(forgotPasswordDTO.getId());
        }

        int otp = otpGenerator();
        forgotPasswordDTO = ForgotPasswordDTO.builder().accountId(account.getId()).otp(otp).expiryDate(new Date(System.currentTimeMillis() + 5 * 60 * 1000)).build();
        forgotPasswordService.createForgotPassword(forgotPasswordDTO);
        emailService.sendMailTime(email, "Reset Password", "Your verification code is: " + otp);
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

            int otp = otpGenerator();
            forgotPasswordDTO = ForgotPasswordDTO.builder().accountId(account.getId()).otp(otp).expiryDate(new Date(System.currentTimeMillis() + 5 * 60 * 1000)).build();
            forgotPasswordService.createForgotPassword(forgotPasswordDTO);
            emailService.sendMailTime(email, "Reset Password", "Your verification code is: " + otp);
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
    public String newpass() {
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
                case Customer -> "redirect:/home";
                case Staff -> "redirect:/staff/home";
                case Cashier -> "redirect:/cashier/home";
                case Customer_Officer -> "redirect:/officer/home";
                default -> "redirect:/newpass";
            };
        }
        model.addAttribute("error", "Something went wrong");
        return "redirect:/newpass";
    }

    public int otpGenerator() {
        Random rand = new Random();
        Integer otp = rand.nextInt(100000, 999999);
        return otp;
    }

}
