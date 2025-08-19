package com.bluebear.cinemax.security;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountService;
import com.bluebear.cinemax.service.customer.CustomerService;
import com.bluebear.cinemax.service.employee.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOauthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmployeeService employeeService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        AccountDTO account = accountService.findAccountByEmail(email);

        if (account.getStatus() == Account_Status.Banned) {
            response.sendRedirect("/login?error=banned");
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("account", account);

        if (account.getRole() == Role.Customer) {
            CustomerDTO customer = customerService.getUserByAccountID(account.getId());
            if (customer == null) {
                response.sendRedirect("/login?error=customer_data_missing");
                return;
            }
            session.setAttribute("customer", customer);
            session.removeAttribute("employee");
        } else if (account.getRole() == Role.Admin || account.getRole() == Role.Cashier || account.getRole() == Role.Customer_Officer || account.getRole() == Role.Staff) {
            EmployeeDTO employee = employeeService.findByAccountId(account.getId());
            if (employee == null) {
                response.sendRedirect("/login?error=employee_data_missing");
                return;
            }
            session.setAttribute("employee", employee);
            session.removeAttribute("customer");
        } else {
            response.sendRedirect("/login?error=unknown_user_type");
            return;
        }

        String redirectUrl = getRedirectUrl(account.getRole());
        response.sendRedirect(redirectUrl);
    }

    private String getRedirectUrl(Role role) {
        return switch (role) {
            case Admin -> "/admin/dashboard";
            case Customer -> "/";
            case Staff -> "/staff/home";
            case Cashier -> "/cashier/movie/";
            case Customer_Officer -> "/officer/blog-management";
            default -> "/";
        };
    }
}