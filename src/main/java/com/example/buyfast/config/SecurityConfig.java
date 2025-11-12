package com.example.buyfast.config;

import com.example.buyfast.modules.user.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepo userRepo;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public UserDetailsService userDetailsService() {
        // This is the new UserDetailsService.
        // It uses your MyBatis findByEmail method, which returns a User
        // object fully populated with roles, permissions, AND profile.
        return email -> userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // --- PUBLIC ENDPOINTS (No change needed) ---
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/categories/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()

                        // --- PERMISSION-BASED ENDPOINTS (THE BIG CHANGE) ---

                        // --- SUPER ADMIN (PLATFORM) ENDPOINTS ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/verify/approve/**", "/api/v1/verify/reject/**")
                        .hasAuthority("APPROVE_VERIFICATION")

                        .requestMatchers("/api/v1/admin/**")
                        .hasAuthority("VIEW_ADMIN_DASHBOARD")

                        // --- COMPANY ADMIN ENDPOINTS ---
                        .requestMatchers("/api/v1/company-admin/**")
                        .hasAuthority("MANAGE_COMPANY_SELLERS")

                        // --- AUTHENTICATED USER ENDPOINTS ---
                        .requestMatchers(HttpMethod.POST, "/api/v1/company")
                        .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/become-seller", "/api/v1/verify/request-user")
                        .authenticated()

                        // --- MODIFIED & EXPANDED PROFILE ENDPOINTS ---
                        .requestMatchers(
                                "/api/v1/user/profile/**", // Catches all /profile endpoints
                                "/api/v1/user/phone/**"    // Catches /phone/send-otp and /phone/verify-otp
                        )
                        .authenticated()

                        // --- DEFAULT: All other requests must be authenticated ---
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}