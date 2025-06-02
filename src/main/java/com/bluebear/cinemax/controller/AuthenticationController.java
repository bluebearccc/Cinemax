package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.service.AccountService;
import com.bluebear.cinemax.service.CustomerService;
import com.bluebear.cinemax.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping
public class AuthenticationController {

    private AccountService accountService;
    private CustomerService customerService;
    private EmployeeService employeeService;


    @Autowired
    public AuthenticationController(AccountService accountService, CustomerService customerService, EmployeeService employeeService) {
        this.accountService = accountService;
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

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
        } else {
            session.setAttribute("account", account);
            CustomerDTO customerDTO = customerService.getUserByAccountID(account.getId());
            if (customerDTO != null) {
                session.setAttribute("customer", customerDTO);
            } else {
                session.setAttribute("employee", employeeService.findByAccountId(account.getId()));
            }

            switch (account.getRole()) {
                case Admin:
                    return "redirect:/admin/dashboard";
                case Customer:
                    return "redirect:/home";
                case Staff:
                    return "redirect:/staff/home";
                case Cashier:
                    return "redirect:/cashier/home";
                case Customer_Officer:
                    return "redirect:/officer/home";
                default:
                    model.addAttribute("error", "Unknown role");
                    return "common/login";
            }
        }
    }

}
