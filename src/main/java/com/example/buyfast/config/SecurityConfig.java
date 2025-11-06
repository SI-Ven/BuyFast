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
    // --- FIELDS FOR THE FILTER AND PROVIDER ARE REMOVED FROM HERE ---

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   // --- INJECT BEANS AS METHOD PARAMETERS ---
                                                   AuthenticationProvider authenticationProvider,
                                                   JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Allow all public endpoints
                        .requestMatchers("/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**")
                        .permitAll()

                        // Allow only admins to approve/reject
                        .requestMatchers(HttpMethod.POST, "/api/v1/verify/approve/**", "/api/v1/verify/reject/**")
                        .hasAnyAuthority("admin_platform", "admin_company")

                        // All other requests must be authenticated
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider) // <-- Use the parameter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // <-- Use the parameter

        return http.build();
    }

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