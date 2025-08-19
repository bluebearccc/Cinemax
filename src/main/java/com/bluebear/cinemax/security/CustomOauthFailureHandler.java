package com.bluebear.cinemax.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomOauthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException {
        String encodedMessage = URLEncoder.encode("oauth2 failed", StandardCharsets.UTF_8);
        Cookie errorCookie = new Cookie("LOGIN_ERROR", encodedMessage);
        errorCookie.setPath("/");
        errorCookie.setMaxAge(30);
        response.addCookie(errorCookie);
        response.sendRedirect("/login");
    }
}
