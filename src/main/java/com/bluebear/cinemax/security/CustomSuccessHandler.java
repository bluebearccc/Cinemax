package com.bluebear.cinemax.security;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountService;
import com.bluebear.cinemax.service.customer.CustomerService;
import com.bluebear.cinemax.service.employee.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountService accountService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private CustomerService customerService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName();
        AccountDTO account = accountService.findAccountByEmail(email);
        HttpSession session = request.getSession();
        session.setAttribute("account", account);
        CustomerDTO customer = customerService.getUserByAccountID(account.getId());
        if (customer != null) {
            session.setAttribute("customer", customer);
        } else {
            EmployeeDTO employee = employeeService.findByAccountId(account.getId());
            session.setAttribute("employee", employee);
        }

        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            String decodedUrl = URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8);
            response.sendRedirect(decodedUrl);
        } else {
            redirectUrl = getRedirectUrl(account.getRole());
            response.sendRedirect(redirectUrl);
        }

    }

    private String getRedirectUrl(Role role) {
        return switch (role) {
            case Admin -> "/admin/";
            case Customer -> "/";
            case Staff -> "/staff/home";
            case Cashier -> "/cashier/movie/";
            case Customer_Officer -> "/officer/blog-management";
            default -> "/";
        };
    }

}
