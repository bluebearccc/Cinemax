
package com.bluebear.cinemax.security;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountServiceImpl;
import com.bluebear.cinemax.service.customer.CustomerServiceImpl;
import com.bluebear.cinemax.service.employee.EmployeeServiceImpl;
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
import java.util.UUID;

@Component
public class CustomOauthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired private AccountServiceImpl accountService;
    @Autowired private CustomerServiceImpl customerService;
    @Autowired private EmployeeServiceImpl employeeService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect("/login?error=invalid_email");
            return;
        }

        HttpSession session = request.getSession();

        AccountDTO account = accountService.findAccountByEmail(email);

        if (account == null) {
            String password = UUID.randomUUID().toString();
            account = AccountDTO.builder()
                    .email(email.trim())
                    .password(password)
                    .role(Role.Customer)
                    .status(Account_Status.Active)
                    .build();

            // Verify the account DTO has email before saving
            if (account.getEmail() == null || account.getEmail().trim().isEmpty()) {
                response.sendRedirect("/login?error=account_creation_failed");
                return;
            }

            accountService.save(account);

            account = accountService.findAccountByEmail(email);

            CustomerDTO customer = CustomerDTO.builder()
                    .accountID(account.getId())
                    .fullName(name != null ? name : "Unknown User")
                    .phone("")
                    .build();
            customerService.save(customer);

            session.setAttribute("customer", customer);
        } else if (account.getStatus() == Account_Status.Banned) {
            response.sendRedirect("/login?error=banned");
            return;
        } else {
            CustomerDTO customer = customerService.getUserByAccountID(account.getId());
            session.setAttribute("customer", customer);
        }

        session.setAttribute("account", account);

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