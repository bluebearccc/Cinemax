package com.bluebear.cinemax.security;

import com.bluebear.cinemax.dto.AccountDTO;
import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.dto.EmployeeDTO;
import com.bluebear.cinemax.enumtype.Account_Status;
import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.service.account.AccountService;
import com.bluebear.cinemax.service.customer.CustomerService;
import com.bluebear.cinemax.service.employee.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private EmployeeService employeeService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("CustomOAuth2UserService.loadUser()\n\n\n");
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User defaultOAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(defaultOAuth2User.getAttributes());

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        System.out.println("name: " + name);
        System.out.println("email: " + email);

        if ("github".equals(registrationId)) {
            String token = userRequest.getAccessToken().getTokenValue();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>("", headers);

            try {
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

                List<Map<String, Object>> emails = response.getBody();
                String primaryEmail = null;
                if (emails != null) {
                    for (Map<String, Object> emailData : emails) {
                        if (Boolean.TRUE.equals(emailData.get("primary"))) {
                            primaryEmail = (String) emailData.get("email");
                            break;
                        }
                    }
                }
                if (primaryEmail != null) {
                    attributes.put("email", primaryEmail);
                    email = primaryEmail;
                }
            } catch (Exception e) {
                System.err.println("Error fetching GitHub emails: " + e.getMessage());
            }
        }

        AccountDTO accountFromDb = accountService.findAccountByEmail(email);
        System.out.println("accountformDB: " + accountFromDb);
        AccountDTO currentAccount;
        CustomerDTO currentCustomer = null;
        EmployeeDTO currentEmployee = null;

        if (accountFromDb == null) {
            String password = UUID.randomUUID().toString();
            AccountDTO newAccount = AccountDTO.builder()
                    .email(email)
                    .password(password)
                    .role(Role.Customer)
                    .status(Account_Status.Active)
                    .build();

            accountService.save(newAccount);
            currentAccount = accountService.findAccountByEmail(email);

            CustomerDTO newCustomer = CustomerDTO.builder()
                    .accountID(currentAccount.getId())
                    .fullName(name != null ? name : "OAuth2 User")
                    .phone("")
                    .build();
            customerService.save(newCustomer);
            currentCustomer = newCustomer;
        } else {
            if (accountFromDb.getStatus() == Account_Status.Banned) {
                throw new OAuth2AuthenticationException("Your account has been banned.");
            }
            currentAccount = accountFromDb;

            // Retrieve associated DTO based on the account's role
            if (currentAccount.getRole() == Role.Customer) {
                currentCustomer = customerService.getUserByAccountID(currentAccount.getId());
            } else if (currentAccount.getRole() == Role.Admin ||
                    currentAccount.getRole() == Role.Staff ||
                    currentAccount.getRole() == Role.Cashier ||
                    currentAccount.getRole() == Role.Customer_Officer) {
                currentEmployee = employeeService.findByAccountId(currentAccount.getId());
            }
        }

        attributes.put("accountDTO", currentAccount);
        attributes.put("accountRole", currentAccount.getRole().name());


        if (currentCustomer != null) {
            attributes.put("customerDTO", currentCustomer);
            attributes.put("userType", "customer");
            System.out.println("\n\n\ncustomer");
        } else if (currentEmployee != null) {
            attributes.put("employeeDTO", currentEmployee);
            attributes.put("userType", "employee");
            System.out.println("\n\n\nemployy");
        } else {
            System.err.println("Warning: Account found but no associated Customer or Employee for email: " + email);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(currentAccount.getRole().name()));

        return new DefaultOAuth2User(
                authorities,
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
        );
    }
}