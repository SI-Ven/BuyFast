package com.example.buyfast.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer; // <-- Import this
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // <-- ENABLE CORS (Fixes 403 from browsers/Swagger)
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ENDPOINTS ---
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/categories/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/error" // <-- CRITICAL FIX: Allow Spring Boot's error page
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,"/api/v1/favorites/**").authenticated()

                        // --- SUPER ADMIN ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/verify/approve/**", "/api/v1/verify/reject/**")
                        .hasAuthority("APPROVE_VERIFICATION")
                        .requestMatchers("/api/v1/admin/**")
                        .hasAuthority("VIEW_ADMIN_DASHBOARD")

                        // --- COMPANY ADMIN ---
                        .requestMatchers("/api/v1/company-admin/**")
                        .hasAuthority("MANAGE_COMPANY_SELLERS")

                        // --- AUTHENTICATED USERS ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/company")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/become-seller", "/api/v1/verify/request-user")
                        .authenticated()
                        .requestMatchers(
                                "/api/v1/user/profile/**",
                                "/api/v1/user/phone/**"
                        )
                        .authenticated()

                        // --- ALL OTHER REQUESTS ---
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}