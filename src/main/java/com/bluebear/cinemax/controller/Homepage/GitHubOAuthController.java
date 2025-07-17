package com.bluebear.cinemax.controller.Homepage;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountServiceImpl;
import com.bluebear.cinemax.service.customer.CustomerServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/oauth/github")
public class GitHubOAuthController {

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    private RestTemplate restTemplate = new RestTemplate();
    private AccountServiceImpl accountService;
    private CustomerServiceImpl customerService;

    @Autowired
    public GitHubOAuthController(AccountServiceImpl accountService, CustomerServiceImpl customerService) {
        this.accountService = accountService;
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public void redirectToGitHub(HttpServletResponse response) throws IOException {
        String url = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
                "&scope=user";
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public String githubCallback(@RequestParam String code, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String accessTokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<?> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(accessTokenUrl, request, Map.class);
        assert response.getBody() != null;
        String accessToken = (String) response.getBody().get("access_token");

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map<String, Object> userInfo = userResponse.getBody();
        HttpEntity<?> emailRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<List> emailResponse = restTemplate.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                emailRequest,
                List.class
        );

        List<Map<String, Object>> emails = emailResponse.getBody();
        String email = null;
        for (Map<String, Object> emailEntry : emails) {
            if ((Boolean) emailEntry.get("primary")) {
                email = (String) emailEntry.get("email");
                break;
            }
        }

        String fullName = (String) userInfo.get("name");

        AccountDTO account = accountService.findAccountByEmail(email);
        if (account == null) {
            String password = UUID.randomUUID().toString();
            account = AccountDTO.builder().email(email).password(password).role(Role.Customer).status(Account_Status.Active).build();
            accountService.save(account);

            account = accountService.findAccountByEmail(email);

            CustomerDTO customer = CustomerDTO.builder().accountID(account.getId()).fullName(fullName).phone("").build();
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
                return "redirect:/";
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

