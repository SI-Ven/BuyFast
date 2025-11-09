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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationProvider authenticationProvider,
                                                   JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow all public endpoints
                        .requestMatchers("/api/v1/auth/**",
                                "/api/v1/categories/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**")
                        .permitAll()

                        // --- SUPER ADMIN ENDPOINTS ---
                        // Only PLATFORM admins can approve/reject ANY verification request
                        .requestMatchers(HttpMethod.POST, "/api/v1/verify/approve/**", "/api/v1/verify/reject/**")
                        .hasAuthority("admin_platform")

                        // --- NEW RULE: Secure the new admin controller ---
                        .requestMatchers("/api/v1/admin/**")
                        .hasAuthority("admin_platform")

                        // --- COMPANY ADMIN ENDPOINTS ---
                        .requestMatchers("/api/v1/company-admin/**")
                        .hasAuthority("admin_company")

                        // --- AUTHENTICATED USER ENDPOINTS ---
                        // Any authenticated user can create a company (it will be pending)
                        .requestMatchers(HttpMethod.POST, "/api/v1/company")
                        .authenticated()
                        // Any authenticated user can request to become a seller or verify their ID
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/become-seller", "/api/v1/verify/request-user")
                        .authenticated()


                        // All other requests must be authenticated
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ... (rest of the file is unchanged) ...
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
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
}