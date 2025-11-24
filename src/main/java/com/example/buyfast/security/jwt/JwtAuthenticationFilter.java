package com.example.buyfast.security.jwt;

import com.example.buyfast.common.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/swagger-config") ||
                path.startsWith("/webjars")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userPublicId;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userPublicId = jwtService.extractPublicId(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userPublicId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.isTokenValid(jwt)) {
                String redisKey = "user_perms:" + userPublicId;
                List<String> permissions = (List<String>) redisService.getValue(redisKey);

                if (permissions != null) {
                    List<SimpleGrantedAuthority> authorities = permissions.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPublicId,
                            null,
                            authorities
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("Cache Miss: User " + userPublicId + " has a valid token, but no session in Redis.");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
