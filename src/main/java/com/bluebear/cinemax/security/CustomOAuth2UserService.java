package com.bluebear.cinemax.security;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2User oAuth2User = super.loadUser(userRequest);

        if ("github".equals(registrationId)) {
            String token = userRequest.getAccessToken().getTokenValue();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
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

            Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
            if (primaryEmail != null) {
                attributes.put("email", primaryEmail);
            }

            return new DefaultOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    "id"
            );
        }

        return oAuth2User;
    }
}



