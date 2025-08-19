package com.bluebear.cinemax.config;

import com.bluebear.cinemax.enumtype.Role;
import com.bluebear.cinemax.security.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private CustomSuccessHandler customSuccessHandler;
    @Autowired private CustomFailureHandler customFailureHandler;
    @Autowired private CustomOauthSuccessHandler customOauthSuccessHandler;
    @Autowired private CustomOauthFailureHandler customOauthFailureHandler;
    @Autowired private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(configurer ->
                        configurer
                                // Public pages
                                .requestMatchers("/", "/login", "/register", "/forgotpassword", "/verifyemail",
                                        "/otp", "/newpass", "/updatepassword", "/verifytoken", "/resendotp",
                                        "/chat/**", "/customer/**", "/home/**", "/test/**", "/uploads/**", "/webhook/**").permitAll()

                                // Static resources
                                .requestMatchers("/static/**", "/customer-static/**", "/common-static/**", "/admin-static/**",
                                        "/staff-static/**", "/cashier-static/**", "/officer-static/**", "/docs/**", "/data/**").permitAll()
                                // OAuth2 endpoints
                                .requestMatchers("/oauth2/**").permitAll()

                                .requestMatchers("/booking/**", "/user/**").hasAuthority(Role.Customer.name())

                                // Admin pages
                                .requestMatchers("/admin/**").hasAuthority(Role.Admin.name())

                                // Staff pages
                                .requestMatchers("/staff/**").hasAuthority(Role.Staff.name())

                                // Cashier pages
                                .requestMatchers("/cashier/**").hasAuthority(Role.Cashier.name())

                                // Customer Officer pages
                                .requestMatchers("/officer/**").hasAuthority(Role.Customer_Officer.name())

                                // Any other request needs authentication
                                .requestMatchers("/vnpay_return/**").permitAll()

                                .anyRequest().authenticated()
                ).formLogin(form ->
                        form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .successHandler(customSuccessHandler)
                                .failureHandler(customFailureHandler)
                                .permitAll()
                ).oauth2Login(oauth2 ->
                        oauth2
                                .loginPage("/login")
                                .authorizationEndpoint(endpoint ->
                                        endpoint.baseUri("/oauth2/authorize"))
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService)
                                        .oidcUserService(oidcUserService())
                                )
                                .successHandler(customOauthSuccessHandler)
                                .failureHandler(customOauthFailureHandler)
                ).logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .deleteCookies("JSESSIONID")
                                .invalidateHttpSession(true)
                                .permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true")
                )
                .rememberMe(remember ->
                        remember
                                .key("cinemax-key")
                                .rememberMeParameter("remember-me")
                                .tokenValiditySeconds(7 * 24 * 60 * 60));


        return http.build();
    }


    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return userRequest -> {
            OAuth2User oAuth2User = customOAuth2UserService.loadUser(userRequest);
            return new DefaultOidcUser(
                    oAuth2User.getAuthorities(),
                    userRequest.getIdToken(),
                    "email"
            );
        };
    }
}
