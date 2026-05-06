package com.internship.tool.config;

import com.internship.tool.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.error("Cannot extract username: {}",
                    e.getMessage());
            }
        }

        if (username != null
                && SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            if (jwtUtil.validateToken(token, username)) {

                // Extract role from token
                String role = jwtUtil.extractRole(token);

                // Log to verify role
                log.info("=== JWT AUTH ===");
                log.info("Username: {}", username);
                log.info("Role from token: {}", role);

                // Build authority with ROLE_ prefix
                List<GrantedAuthority> authorities =
                    new ArrayList<>();

                // Add exact role
                authorities.add(
                    new SimpleGrantedAuthority(
                        "ROLE_" + role.toUpperCase()));

                log.info("Authorities: {}", authorities);

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                        .buildDetails(request));

                SecurityContextHolder.getContext()
                    .setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}