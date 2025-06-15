package com.bluebear.cinemax.controller;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountService;
import com.bluebear.cinemax.service.customer.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/oauth/google")
public class GoogleOAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AccountService accountService;
    private final CustomerService customerService;

    public GoogleOAuthController(AccountService accountService, CustomerService customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&response_type=code" +
                "&scope=" + URLEncoder.encode("openid email profile", "UTF-8");
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public String googleCallback(@RequestParam String code, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // B1: Lấy access_token
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        HttpEntity<?> tokenRequest = new HttpEntity<>(body, headers);
        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // B2: Lấy thông tin người dùng từ Google
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map<String, Object> userInfo = userResponse.getBody();
        String email = (String) userInfo.get("email");
        String fullName = (String) userInfo.get("name");

        AccountDTO account = accountService.findAccountByEmail(email);
        if (account == null) {
            String password = UUID.randomUUID().toString();
            account = new AccountDTO(email, password, Role.Customer, Account_Status.Active);
            accountService.save(account);

            account = accountService.findAccountByEmail(email);

            CustomerDTO customer = new CustomerDTO(account.getId(), fullName, "");
            customerService.save(customer);

            session.setAttribute("customer", customer);
        } else if (account.getStatus() == Account_Status.Banned) {
            redirectAttributes.addFlashAttribute("error", "Opps! this account has been banned.");
            return "redirect:/login";
        } else {
            CustomerDTO customer = customerService.getUserByAccountID(account.getId());
            session.setAttribute("customer", customer);
        }

        session.setAttribute("account", account);

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


