package com.example.buyfast.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Import
import org.springframework.web.cors.CorsConfigurationSource; // Import
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Import

import java.util.List; // Import

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
                .cors(Customizer.withDefaults()) // Uses the corsConfigurationSource bean defined below
                .authorizeHttpRequests(auth -> auth
                        // --- PUBLIC ENDPOINTS ---
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/categories/**",
                                "/api/v1/products/public",
                                "/api/v1/products/search",
                                "/api/v1/products/search/image",
                                "api/v1/products/{productId}",
                                "api/v1/products/public/**",
                                "/ws/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/error"
                        ).permitAll()


                        .requestMatchers(HttpMethod.POST,"/api/v1/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/v1/shipping-address/**").authenticated()
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

    // --- NEW BEAN TO FIX "FAILED TO FETCH" / CORS ERRORS ---
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Allow your frontend origin (Adjust port if your Next.js runs on something else)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // 2. Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. Allow all headers (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));

        // 4. Allow credentials (cookies/auth headers) if needed
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}