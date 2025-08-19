package com.bluebear.cinemax.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Invalid email or password";

        if (exception.getMessage().contains("banned")) {
            errorMessage = "This account has been banned";
        }

        // Mã hóa nội dung cookie
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        Cookie errorCookie = new Cookie("LOGIN_ERROR", encodedMessage);
        errorCookie.setPath("/");
        errorCookie.setMaxAge(30);
        response.addCookie(errorCookie);
        response.sendRedirect("/login");
    }
}
